package io.github.wimdeblauwe.ttcli.livereload.devtools;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceException;
import io.github.wimdeblauwe.ttcli.livereload.helper.TailwindCssHelper;
import io.github.wimdeblauwe.ttcli.maven.MavenPomReaderWriter;
import io.github.wimdeblauwe.ttcli.npm.InstalledApplicationVersions;
import io.github.wimdeblauwe.ttcli.npm.NodeService;
import org.jsoup.nodes.Comment;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

@Component
@Order(4)
public class DevToolsBasedWithTailwindCssLiveReloadInitService implements LiveReloadInitService {
    private final NodeService nodeService;

    public DevToolsBasedWithTailwindCssLiveReloadInitService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public String getId() {
        return "dev-tools-based-with-tailwind-css";
    }

    @Override
    public String getName() {
        return "DevTools based with Tailwind CSS (Uses NPM)";
    }

    @Override
    public String getHelpText() {
        return """
                # Live reload setup
                                
                This project uses Spring Boot DevTools to have live reloading.
                                
                Use the following steps to get it working:
                                
                1. Install the LiveReload browser extension in your browser.
                2. Configure your editor to automatically compile when saving. For IntelliJ, you need to enable 'Build project automatically' in the project settings.
                   Also enable 'Allow auto-make to start even if developed application is currently running'.
                3. Run `npm run watch` in the `src/main/frontend` directory to ensure the Tailwind CSS output file is generated.
                3. Run the Spring Boot application.
                4. Open the browser at http://localhost:8080. Ensure the Live Reload extension is active in the browser.
                                
                You should now be able to change any HTML or CSS and have the browser reload upon saving the file.
                """;
    }

    @Override
    public Set<String> additionalSpringInitializrDependencies() {
        return Set.of("devtools");
    }

    @Override
    public void generate(ProjectInitializationParameters projectInitializationParameters) throws LiveReloadInitServiceException {
        try {

            InstalledApplicationVersions installedApplicationVersions = nodeService.checkIfNodeAndNpmAreInstalled();
            Path basePath = projectInitializationParameters.basePath();
            Path frontendBasePath = basePath.resolve("src/main/frontend");
            if (!Files.exists(frontendBasePath)) {
                Files.createDirectories(frontendBasePath);
            }

            nodeService.createEmptyPackageJson(frontendBasePath,
                                               projectInitializationParameters.projectName());
            nodeService.installNpmDevDependencies(frontendBasePath,
                                                  List.of("tailwindcss"));
            nodeService.insertPackageJsonScripts(frontendBasePath, npmScripts());

            MavenPomReaderWriter mavenPomReaderWriter = MavenPomReaderWriter.readFrom(basePath);
            updateMavenPom(mavenPomReaderWriter, installedApplicationVersions);
            updateGitIgnore(basePath);


            TailwindCssHelper.createApplicationCss(basePath,
                                                   "src/main/frontend/application.css");
            TailwindCssHelper.setupTailwindConfig(frontendBasePath,
                                                  "../resources/templates/**/*.{html,js}");
        } catch (IOException e) {
            throw new LiveReloadInitServiceException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LiveReloadInitServiceException(e);
        }
    }

    @Override
    public void runBuild(ProjectInitializationParameters projectInitializationParameters) throws LiveReloadInitServiceException {
    }

    private LinkedHashMap<String, String> npmScripts() {
        LinkedHashMap<String, String> scripts = new LinkedHashMap<>();
        scripts.put("build", "tailwindcss -i ./application.css -o ../resources/static/css/application.css");
        scripts.put("watch", "tailwindcss --watch -i ./application.css -o ../resources/static/css/application.css");
        return scripts;
    }

    private void updateMavenPom(MavenPomReaderWriter mavenPomReaderWriter,
                                InstalledApplicationVersions versions) throws IOException {
        System.out.println("\uD83D\uDC77\uD83C\uDFFB\u200D♀️ Updating Maven pom.xml");
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
                                       <configuration>
                                            <nodeVersion>${frontend-maven-plugin.nodeVersion}</nodeVersion>
                                            <workingDirectory>src/main/frontend</workingDirectory>
                                       		<installDirectory>target</installDirectory>
                                       </configuration>
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

        mavenPomReaderWriter.write();
    }

    private void updateGitIgnore(Path base) throws IOException {
        Path path = base.resolve(".gitignore");
        Files.createDirectories(path.getParent());
        String s = """
                                
                # Excludes for npm
                src/main/frontend/node_modules
                src/main/resources/css/application.css
                """;
        if (!Files.exists(path)) {
            Files.writeString(path, s, StandardOpenOption.CREATE);
        } else {
            Files.writeString(path, s, StandardOpenOption.APPEND);
        }
    }

}
