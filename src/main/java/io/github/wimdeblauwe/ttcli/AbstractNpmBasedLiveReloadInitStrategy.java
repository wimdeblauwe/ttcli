package io.github.wimdeblauwe.ttcli;

import org.jsoup.nodes.Comment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public abstract class AbstractNpmBasedLiveReloadInitStrategy implements LiveReloadInitStrategy {

    protected AbstractNpmBasedLiveReloadInitStrategy() {
    }

    @Override
    public void execute(LiveReloadInitParameters parameters) throws IOException, InterruptedException {
        InstalledApplicationVersions versions = checkIfNodeAndNpmAreInstalled();

        Path base = parameters.baseDir();
        MavenPomReaderWriter mavenPomReaderWriter = MavenPomReaderWriter.readFrom(base);

        createEmptyPackageJson(base, mavenPomReaderWriter.getProjectArtifactId());
        installNpmDependencies(base);
        copyCopyFilesJs(base);
        copyPostcssConfigJs(base);
        insertPackageJsonScripts(base);
        createApplicationCss(base);
        postExecuteNpmPart(base);

        updateMavenPom(mavenPomReaderWriter, versions);
    }

    protected void postExecuteNpmPart(Path base) throws IOException, InterruptedException {

    }

    protected abstract List<String> npmDependencies();

    protected abstract LinkedHashMap<String, String> npmScripts();

    protected abstract String applicationCssContent();

    protected abstract String postcssConfigJsSourceFile();

    private void createApplicationCss(Path base) throws IOException {
        Path path = base.resolve("src/main/resources/static/css/application.css");
        Files.createDirectories(path.getParent());
        Files.writeString(path, applicationCssContent());
    }

    private void insertPackageJsonScripts(Path base) throws IOException, InterruptedException {
        System.out.println("\uD83D\uDC77\u200D♂️ Adding npm build scripts");
        installNpmAddScriptDependency(base);

        addBuildScriptsToPackageJson(base);

        uninstallNpmAddScriptDependency(base);
    }

    private void addBuildScriptsToPackageJson(Path base) throws IOException, InterruptedException {
        for (Map.Entry<String, String> entry : npmScripts().entrySet()) {
            ProcessBuilder builder = new ProcessBuilder("npx", "npmAddScript", "-k", entry.getKey(), "-v", entry.getValue());
            builder.directory(base.toFile());
            Process process = builder.start();
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                throw new RuntimeException("unable to add script entry " + entry + "\n" + new String(process.getErrorStream().readAllBytes()));
            }
        }
    }

    private void installNpmAddScriptDependency(Path base) throws InterruptedException, IOException {
        ProcessBuilder builder = new ProcessBuilder("npm", "install", "-D", "npm-add-script");
        builder.directory(base.toFile());
        int exitValue = builder.start().waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("unable to install npm-add-script");
        }
    }

    private void uninstallNpmAddScriptDependency(Path base) throws InterruptedException, IOException {
        ProcessBuilder builder = new ProcessBuilder("npm", "remove", "-D", "npm-add-script");
        builder.directory(base.toFile());
        int exitValue = builder.start().waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("unable to remove npm-add-script");
        }
    }

    private void copyPostcssConfigJs(Path base) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(postcssConfigJsSourceFile())) {
            Files.copy(Objects.requireNonNull(stream, () -> "Could not find " + postcssConfigJsSourceFile()),
                       base.resolve("postcss.config.js"));
        }
    }

    private void copyCopyFilesJs(Path base) throws IOException {
        String source = "/files/copy-files.js";
        try (InputStream stream = getClass().getResourceAsStream(source)) {
            Files.copy(Objects.requireNonNull(stream, () -> "Could not find " + source),
                       base.resolve("copy-files.js"));
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
        return new InstalledApplicationVersions(nodeVersion,
                                                npmVersion);
    }

    private String checkIfApplicationIsInstalled(String application) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(application, "-v");
        Process process = builder.start();
        int exitValue = process.waitFor();
        String version = new String(process.getInputStream().readAllBytes()).trim();
        System.out.println("\uD83D\uDEE0️  Using " + application + " " + version);
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
}
