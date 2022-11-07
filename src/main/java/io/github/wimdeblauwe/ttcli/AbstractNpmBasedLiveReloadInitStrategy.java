package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import io.github.wimdeblauwe.ttcli.maven.MavenDependency;
import io.github.wimdeblauwe.ttcli.npm.PackageJsonReaderWriter;
import org.jsoup.nodes.Comment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public abstract class AbstractNpmBasedLiveReloadInitStrategy implements LiveReloadInitStrategy {

    private final List<WebDependency> webDependencies;

    protected AbstractNpmBasedLiveReloadInitStrategy(List<WebDependency> webDependencies) {
        this.webDependencies = webDependencies;
    }

    @Override
    public void execute(LiveReloadInitParameters parameters) throws IOException, InterruptedException {
        InstalledApplicationVersions versions = checkIfNodeAndNpmAreInstalled();

        Path base = parameters.baseDir();
        MavenPomReaderWriter mavenPomReaderWriter = MavenPomReaderWriter.readFrom(base);

        createEmptyPackageJson(base, mavenPomReaderWriter.getProjectArtifactId());
        installNpmDependencies(base);
        copyPostcssConfigJs(base);
        insertPackageJsonScripts(base);
        createApplicationCss(base);
        postExecuteNpmPart(base);

        updateMavenPom(mavenPomReaderWriter, versions);
        updateSpringApplicationProperties(base);
        addMavenDependencies(mavenPomReaderWriter);
        createDefaultTemplates(base);
    }

    protected void postExecuteNpmPart(Path base) throws IOException, InterruptedException {

    }

    protected abstract List<String> npmDependencies();

    protected abstract LinkedHashMap<String, String> npmScripts();

    protected abstract String applicationCssContent();

    protected abstract String postcssConfigJsSourceFile();

    protected void doAddMavenDependencies(MavenPomReaderWriter mavenPomReaderWriter) {
    }

    protected String getCssLinksForLayoutTemplate() {
        return "";
    }

    protected String getJsLinksForLayoutTemplate() {
        return "";
    }

    private void createApplicationCss(Path base) throws IOException {
        Path path = base.resolve("src/main/resources/static/css/application.css");
        Files.createDirectories(path.getParent());
        Files.writeString(path, applicationCssContent());
    }

    private void insertPackageJsonScripts(Path base) throws IOException, InterruptedException {
        System.out.println("\uD83D\uDC77\u200D♂️ Adding npm build scripts");
        addBuildScriptsToPackageJson(base);
    }

    private void addBuildScriptsToPackageJson(Path base) throws IOException {
        PackageJsonReaderWriter packageJsonReaderWriter = PackageJsonReaderWriter.readFrom(base.resolve("package.json"));
        packageJsonReaderWriter.addScripts(npmScripts());
        packageJsonReaderWriter.write();
    }

    private void copyPostcssConfigJs(Path base) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(postcssConfigJsSourceFile())) {
            Files.copy(Objects.requireNonNull(stream, () -> "Could not find " + postcssConfigJsSourceFile()),
                       base.resolve("postcss.config.js"));
        }
    }

    private void installNpmDependencies(Path base) throws IOException, InterruptedException {
        System.out.println("\uD83D\uDD28 Installing npm dependencies");
        List<String> parameters = new ArrayList<>();
        parameters.addAll(List.of("npm", "install", "-D"));
        parameters.addAll(npmDependencies());
        ProcessBuilder builder = new ProcessBuilder(parameters);
        builder.directory(base.toFile());
        int exitValue = builder.start().waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("installation of npm dependencies failed");
        }
    }

    private void createEmptyPackageJson(Path base,
                                        String projectArtifactId) throws IOException {
        Path path = base.resolve("package.json");
        Files.writeString(path, """
                {
                  "name": "%s"
                }
                """.formatted(projectArtifactId));
    }

    private InstalledApplicationVersions checkIfNodeAndNpmAreInstalled() throws IOException, InterruptedException {
        String nodeVersion = checkIfApplicationIsInstalled("node");
        String npmVersion = checkIfApplicationIsInstalled("npm");
        System.out.println("\uD83D\uDEE0️  Using node " + nodeVersion + " with npm " + npmVersion);
        return new InstalledApplicationVersions(nodeVersion,
                                                npmVersion);
    }

    private String checkIfApplicationIsInstalled(String application) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(application, "-v");
        Process process = builder.start();
        int exitValue = process.waitFor();
        String version = new String(process.getInputStream().readAllBytes()).trim();
        if (exitValue != 0) {
            throw new IllegalArgumentException(application + " is not installed");
        }

        return version;
    }

    private void updateMavenPom(MavenPomReaderWriter mavenPomReaderWriter,
                                InstalledApplicationVersions versions) throws IOException {
        System.out.println("\uD83D\uDC77\uD83C\uDFFB\u200D♀️ Updating Maven pom.xml");
        mavenPomReaderWriter.updateResources(resources -> resources.append("""
                                                                                   <resource>
                                                                                       <directory>src/main/resources</directory>
                                                                                       <excludes>
                                                                                           <exclude>**/*.html</exclude>
                                                                                           <exclude>**/*.css</exclude>
                                                                                           <exclude>**/*.js</exclude>
                                                                                           <exclude>**/*.svg</exclude>
                                                                                       </excludes>
                                                                                   </resource>
                                                                                                       """));
        mavenPomReaderWriter.updateProperties(properties -> {
            properties.appendChild(new Comment(" Maven plugins "));
            properties.appendElement("frontend-maven-plugin.version").text("1.12.1");
            properties.appendElement("frontend-maven-plugin.nodeVersion").text(versions.nodeVersion());
            properties.appendElement("frontend-maven-plugin.npmVersion").text(versions.npmVersion());
        });

        mavenPomReaderWriter.updatePluginManagementPlugins(plugins -> {
            plugins.append("""
                                   <plugin>
                                       <groupId>com.github.eirslett</groupId>
                                       <artifactId>frontend-maven-plugin</artifactId>
                                       <version>${frontend-maven-plugin.version}</version>
                                       <executions>
                                           <execution>
                                               <id>install-frontend-tooling</id>
                                               <goals>
                                                   <goal>install-node-and-npm</goal>
                                               </goals>
                                               <configuration>
                                                   <nodeVersion>${frontend-maven-plugin.nodeVersion}</nodeVersion>
                                                   <npmVersion>${frontend-maven-plugin.npmVersion}</npmVersion>
                                               </configuration>
                                           </execution>
                                           <execution>
                                               <id>run-npm-install</id>
                                               <goals>
                                                   <goal>npm</goal>
                                               </goals>
                                           </execution>
                                           <execution>
                                               <id>run-npm-build</id>
                                               <goals>
                                                   <goal>npm</goal>
                                               </goals>
                                               <configuration>
                                                   <arguments>run build</arguments>
                                               </configuration>
                                           </execution>
                                       </executions>
                                   </plugin>""");
        });

        mavenPomReaderWriter.updateBuildPlugins(plugins -> {
            plugins.append("""
                                   <plugin>
                                       <groupId>com.github.eirslett</groupId>
                                       <artifactId>frontend-maven-plugin</artifactId>
                                   </plugin>""");
        });

        mavenPomReaderWriter.updateProfiles(profiles -> {
            profiles.append("""
                                    <profile>
                                        <id>release</id>
                                        <build>
                                            <plugins>
                                                <plugin>
                                                    <groupId>com.github.eirslett</groupId>
                                                    <artifactId>frontend-maven-plugin</artifactId>
                                                    <executions>
                                                        <execution>
                                                            <id>run-npm-build</id>
                                                            <goals>
                                                                <goal>npm</goal>
                                                            </goals>
                                                            <configuration>
                                                                <arguments>run build-prod</arguments>
                                                            </configuration>
                                                        </execution>
                                                    </executions>
                                                </plugin>
                                            </plugins>
                                        </build>
                                    </profile>""");
        });

        mavenPomReaderWriter.write();
    }

    private void updateSpringApplicationProperties(Path base) throws IOException {
        Path propertiesFile = base.resolve("src/main/resources/application-local.properties");
        Files.createDirectories(propertiesFile.getParent());
        String s = """
                spring.thymeleaf.cache=false
                spring.web.resources.chain.cache=false
                """;
        if (!Files.exists(propertiesFile)) {
            Files.writeString(propertiesFile, s, StandardOpenOption.CREATE);
        } else {
            Files.writeString(propertiesFile, s, StandardOpenOption.APPEND);
        }
    }

    private void addMavenDependencies(MavenPomReaderWriter mavenPomReaderWriter) throws IOException {
        mavenPomReaderWriter.addDependency("nz.net.ultraq.thymeleaf", "thymeleaf-layout-dialect");
        mavenPomReaderWriter.updateDependencies(dependencies -> {
            dependencies.appendChild(new Comment(" Web dependencies "));
        });
        mavenPomReaderWriter.addDependency("org.webjars", "webjars-locator", "0.41");
        doAddMavenDependencies(mavenPomReaderWriter);
        for (WebDependency webDependency : webDependencies) {
            List<MavenDependency> mavenDependencies = webDependency.getMavenDependencies();
            for (MavenDependency mavenDependency : mavenDependencies) {
                mavenPomReaderWriter.addDependency(mavenDependency);
            }
        }

        mavenPomReaderWriter.write();
    }

    private void createDefaultTemplates(Path base) throws IOException {
        createDefaultLayoutTemplate(base);
        createDefaultIndexTemplate(base);
    }

    private void createDefaultLayoutTemplate(Path base) throws IOException {
        Path layoutTemplate = base.resolve("src/main/resources/templates/layout/main.html");
        Files.createDirectories(layoutTemplate.getParent());
        String source = "/files/templates/layout/main.html";
        try (InputStream stream = getClass().getResourceAsStream(source)) {
            Files.copy(Objects.requireNonNull(stream, () -> "Could not find " + source),
                       layoutTemplate);
        }

        StringBuilder cssLinksForLayoutTemplate = new StringBuilder(getCssLinksForLayoutTemplate());
        for (WebDependency webDependency : webDependencies) {
            String cssForDependency = webDependency.getCssLinksForLayoutTemplate();
            if( cssForDependency != null) {
                cssLinksForLayoutTemplate
                        .append('\n')
                        .append(cssForDependency);
            }
        }
        insertCssLinksToLayoutTemplate(layoutTemplate, cssLinksForLayoutTemplate.toString());

        StringBuilder jsLinksForLayoutTemplate = new StringBuilder(getJsLinksForLayoutTemplate());
        for (WebDependency webDependency : webDependencies) {
            String jsForDependency = webDependency.getJsLinksForLayoutTemplate();
            if( jsForDependency != null) {
                jsLinksForLayoutTemplate
                        .append('\n')
                        .append(jsForDependency);
            }
        }
        insertJsLinksToLayoutTemplate(layoutTemplate, jsLinksForLayoutTemplate.toString());
    }

    private void insertCssLinksToLayoutTemplate(Path layoutTemplate,
                                                String cssLinksForLayoutTemplate) throws IOException {
        String layoutTemplateContent = Files.readString(layoutTemplate);
        String result = layoutTemplateContent.replace("<!-- REPLACE WITH CSS LINKS -->",
                                                      cssLinksForLayoutTemplate);
        Files.writeString(layoutTemplate, result);
    }

    private void insertJsLinksToLayoutTemplate(Path layoutTemplate,
                                               String jsLinksForLayoutTemplate) throws IOException {
        String layoutTemplateContent = Files.readString(layoutTemplate);
        String result = layoutTemplateContent.replace("<!-- REPLACE WITH JS LINKS -->",
                                                      jsLinksForLayoutTemplate);
        Files.writeString(layoutTemplate, result);
    }

    private void createDefaultIndexTemplate(Path base) throws IOException {
        Path indexTemplate = base.resolve("src/main/resources/templates/index.html");
        Files.createDirectories(indexTemplate.getParent());
        String source = "/files/templates/index.html";
        try (InputStream stream = getClass().getResourceAsStream(source)) {
            Files.copy(Objects.requireNonNull(stream, () -> "Could not find " + source),
                       indexTemplate);
        }
    }

}
