package io.github.wimdeblauwe.ttcli.livereload.npm;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceException;
import io.github.wimdeblauwe.ttcli.maven.MavenPomReaderWriter;
import io.github.wimdeblauwe.ttcli.npm.InstalledApplicationVersions;
import io.github.wimdeblauwe.ttcli.npm.NodeService;
import io.github.wimdeblauwe.ttcli.util.ProcessBuilderFactory;
import org.jsoup.nodes.Comment;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

@Component
@Order(1)
public class NpmBasedLiveReloadInitService implements LiveReloadInitService {
    private final NodeService nodeService;

    public NpmBasedLiveReloadInitService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public String getId() {
        return "npm-based";
    }

    @Override
    public String getName() {
        return "NPM based";
    }

    @Override
    public String getHelpText() {
        return """
                # Live reload setup
                                
                This project uses NPM to have live reloading.
                                
                Use the following steps to get it working:
                                
                1. Run the Spring Boot application with the `local` profile
                2. From a terminal, run `npm run build && npm run watch` (You can also run `npm run --silent build && npm run --silent watch` if you want less output in the terminal)
                3. Your default browser will open at http://localhost:3000
                                
                You should now be able to change any HTML or CSS and have the browser reload upon saving the file.
                                
                NOTE: If you use a separate authentication server (e.g. social logins, or Keycloak) then after login,
                you might get redirected to http://localhost:8080 as opposed to http://localhost:3000.
                Be sure to set the port back to `3000` in your browser to have live reload.""";
    }

    @Override
    public Set<String> additionalSpringInitializrDependencies() {
        return Collections.emptySet();
    }

    @Override
    public void generate(ProjectInitializationParameters projectInitializationParameters) throws LiveReloadInitServiceException {
        try {
            InstalledApplicationVersions installedApplicationVersions = nodeService.checkIfNodeAndNpmAreInstalled();
            Path basePath = projectInitializationParameters.basePath();
            nodeService.createEmptyPackageJson(basePath,
                                               projectInitializationParameters.projectName());
            nodeService.installNpmDevDependencies(basePath,
                                                  npmDevDependencies());
            copyPostcssConfigJs(basePath,
                                postcssConfigFilePath());
            nodeService.insertPackageJsonScripts(basePath, npmScripts());

            MavenPomReaderWriter mavenPomReaderWriter = MavenPomReaderWriter.readFrom(basePath);
            updateMavenPom(mavenPomReaderWriter, installedApplicationVersions);
            updateSpringApplicationProperties(basePath);
            updateGitIgnore(basePath);

        } catch (IOException e) {
            throw new LiveReloadInitServiceException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LiveReloadInitServiceException(e);
        }
    }

    @Override
    public void runBuild(ProjectInitializationParameters projectInitializationParameters) throws LiveReloadInitServiceException {
        ProcessBuilder builder = ProcessBuilderFactory.create(List.of("npm", "run", "build"));
        builder.directory(projectInitializationParameters.basePath().toFile());
        int exitValue;
        try {
            exitValue = builder.start().waitFor();
        } catch (IOException e) {
            throw new LiveReloadInitServiceException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LiveReloadInitServiceException(e);
        }
        if (exitValue != 0) {
            throw new RuntimeException("unable to init tailwind css");
        }
    }

    @Override
    public Path getTailwindConfigFileParentPath(ProjectInitializationParameters parameters) {
        return null;
    }

    protected String postcssConfigFilePath() {
        return "/files/livereload/npm/npm-based/postcss.config.js";
    }

    protected List<String> npmDevDependencies() {
        return List.of("@babel/cli", "autoprefixer", "browser-sync", "cssnano",
                       "mkdirp", "npm-run-all", "onchange", "postcss", "postcss-cli", "recursive-copy-cli", "path-exists-cli");
    }

    protected LinkedHashMap<String, String> npmScripts() {
        LinkedHashMap<String, String> scripts = new LinkedHashMap<>();
        scripts.put("build", "npm-run-all --parallel build:*");
        scripts.put("build:html", "recursive-copy \"src/main/resources/templates\" target/classes/templates -w");
        scripts.put("build:css", "mkdirp target/classes/static/css && postcss src/main/resources/static/css/*.css -d target/classes/static/css");
        scripts.put("build:js", "path-exists src/main/resources/static/js && (mkdirp target/classes/static/js && babel src/main/resources/static/js/ --out-dir target/classes/static/js/) || echo \"No 'src/main/resources/static/js' directory found.\"");
        scripts.put("build:svg", "path-exists src/main/resources/static/svg && recursive-copy \"src/main/resources/static/svg\" target/classes/static/svg -w -f \"**/*.svg\" || echo \"No 'src/main/resources/static/svg' directory found.\"");
        scripts.put("build-prod", "NODE_ENV='production' npm-run-all --parallel build-prod:*");
        scripts.put("build-prod:html", "npm run build:html");
        scripts.put("build-prod:css", "npm run build:css");
        scripts.put("build-prod:js", "path-exists src/main/resources/static/js && (mkdirp target/classes/static/js && babel src/main/resources/static/js/ --minified --out-dir target/classes/static/js/) || echo \"No 'src/main/resources/static/js' directory found.\"");
        scripts.put("build-prod:svg", "npm run build:svg");
        scripts.put("watch", "npm-run-all --parallel watch:*");
        scripts.put("watch:html", "onchange \"src/main/resources/templates/**/*.html\" -- npm run build:html");
        scripts.put("watch:css", "onchange \"src/main/resources/static/css/**/*.css\" -- npm run build:css");
        scripts.put("watch:js", "onchange \"src/main/resources/static/js/**/*.js\" -- npm run build:js");
        scripts.put("watch:svg", "onchange \"src/main/resources/static/svg/**/*.svg\" -- npm run build:svg");
        scripts.put("watch:serve", "browser-sync start --proxy localhost:8080 --files \"target/classes/templates\" \"target/classes/static\"");
        return scripts;
    }

    private void copyPostcssConfigJs(Path base,
                                     String sourceFile) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(sourceFile)) {
            Files.copy(Objects.requireNonNull(stream, () -> "Could not find " + sourceFile),
                       base.resolve("postcss.config.js"));
        }
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
            properties.appendElement("frontend-maven-plugin.version").text("1.15.0");
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

    private void updateGitIgnore(Path base) throws IOException {
        Path path = base.resolve(".gitignore");
        Files.createDirectories(path.getParent());
        String s = """
                
                # Excludes for npm
                node_modules
                node
                """;
        if (!Files.exists(path)) {
            Files.writeString(path, s, StandardOpenOption.CREATE);
        } else {
            Files.writeString(path, s, StandardOpenOption.APPEND);
        }
    }

}
