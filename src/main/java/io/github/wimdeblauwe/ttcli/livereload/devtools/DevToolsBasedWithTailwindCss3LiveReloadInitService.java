package io.github.wimdeblauwe.ttcli.livereload.devtools;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceException;
import io.github.wimdeblauwe.ttcli.livereload.TailwindCssSpecializedLiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.helper.NpmHelper;
import io.github.wimdeblauwe.ttcli.livereload.helper.TailwindCss3Helper;
import io.github.wimdeblauwe.ttcli.maven.MavenPomReaderWriter;
import io.github.wimdeblauwe.ttcli.npm.InstalledApplicationVersions;
import io.github.wimdeblauwe.ttcli.npm.NodeService;
import io.github.wimdeblauwe.ttcli.npm.PackageManager;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindVersion;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.jsoup.nodes.Comment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

@Component
public class DevToolsBasedWithTailwindCss3LiveReloadInitService implements LiveReloadInitService, TailwindCssSpecializedLiveReloadInitService {
    private final NodeService nodeService;

    public DevToolsBasedWithTailwindCss3LiveReloadInitService(NodeService nodeService) {
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
        return getHelpText(PackageManager.NPM);
    }

    @Override
    public String getHelpText(PackageManager packageManager) {
        String pm = packageManager.executable();
        return """
                # Live reload setup

                This project uses Spring Boot DevTools to have live reloading.

                Use the following steps to get it working:

                1. Install the LiveReload browser extension in your browser.
                2. Configure your editor to automatically compile when saving. For IntelliJ, you need to enable 'Build project automatically' in the project settings.
                   Also enable 'Allow auto-make to start even if developed application is currently running'.
                3. Run `%s run watch` in the `src/main/frontend` directory to ensure the Tailwind CSS output file is generated.
                3. Run the Spring Boot application.
                4. Open the browser at http://localhost:8080. Ensure the Live Reload extension is active in the browser.

                You should now be able to change any HTML or CSS and have the browser reload upon saving the file.
                """.formatted(pm);
    }

    @Override
    public Set<String> additionalSpringInitializrDependencies() {
        return Set.of("devtools");
    }

    @Override
    public void generate(ProjectInitializationParameters projectInitializationParameters) throws LiveReloadInitServiceException {
        try {
            PackageManager packageManager = projectInitializationParameters.packageManager();
            InstalledApplicationVersions installedApplicationVersions = nodeService.checkIfNodeAndPackageManagerAreInstalled(packageManager);
            Path basePath = projectInitializationParameters.basePath();
            Path frontendBasePath = basePath.resolve("src/main/frontend");
            if (!Files.exists(frontendBasePath)) {
                Files.createDirectories(frontendBasePath);
            }

            nodeService.createPackageJson(frontendBasePath,
                    projectInitializationParameters.projectName());
            nodeService.installDevDependencies(packageManager, frontendBasePath,
                    List.of("tailwindcss@3"));
            nodeService.insertPackageJsonScripts(frontendBasePath, NpmHelper.rewriteScriptsForPackageManager(packageManager, npmScripts()));

            MavenPomReaderWriter mavenPomReaderWriter = MavenPomReaderWriter.readFrom(basePath);
            updateMavenPom(mavenPomReaderWriter, installedApplicationVersions);
            updateGitIgnore(basePath);


            TailwindCss3Helper.createApplicationCss(basePath,
                    "src/main/frontend/application.css");
            TailwindCss3Helper.setupTailwindConfig(frontendBasePath,
                    "../resources/templates/**/*.{html,js}");

            if (packageManager == PackageManager.PNPM) {
                NpmHelper.applyPnpmOnlyBuiltDependencies(frontendBasePath);
            }
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

    @Override
    public Path getTailwindConfigFileParentPath(ProjectInitializationParameters parameters) {
        return parameters.basePath().resolve("src/main/frontend");
    }

    @Override
    public boolean isApplicableForTemplateEngine(TemplateEngineType templateEngineType) {
        return templateEngineType.equals(TemplateEngineType.THYMELEAF);
    }

    private LinkedHashMap<String, String> npmScripts() {
        LinkedHashMap<String, String> scripts = new LinkedHashMap<>();
        scripts.put("build", "tailwindcss -i ./application.css -o ../resources/static/css/application.css");
        scripts.put("watch", "tailwindcss --watch -i ./application.css -o ../resources/static/css/application.css");
        return scripts;
    }

    private void updateMavenPom(MavenPomReaderWriter mavenPomReaderWriter,
                                InstalledApplicationVersions versions) throws IOException, InterruptedException {
        System.out.println("👷🏻‍♀️ Updating Maven pom.xml");
        PackageManager packageManager = versions.packageManager();
        String installGoal = packageManager.frontendMavenPluginInstallGoal();
        String runGoal = packageManager.frontendMavenPluginRunGoal();
        String versionPropertyName = packageManager.mavenVersionProperty();
        String installVersionConfig = packageManager == PackageManager.PNPM
                ? "<pnpmVersion>${" + versionPropertyName + "}</pnpmVersion>"
                : "<npmVersion>${" + versionPropertyName + "}</npmVersion>";

        mavenPomReaderWriter.updateProperties(properties -> {
            properties.appendChild(new Comment(" Maven plugins "));
            properties.appendElement("frontend-maven-plugin.version").text("1.15.0");
            properties.appendElement("frontend-maven-plugin.nodeVersion").text(versions.nodeVersion());
            properties.appendElement(versionPropertyName).text(versions.packageManagerVersion());
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
                                    <goal>%s</goal>
                                </goals>
                                <configuration>
                                    <nodeVersion>${frontend-maven-plugin.nodeVersion}</nodeVersion>
                                    %s
                                </configuration>
                            </execution>
                            <execution>
                                <id>run-%s-install</id>
                                <goals>
                                    <goal>%s</goal>
                                </goals>
                            </execution>
                            <execution>
                                <id>run-%s-build</id>
                                <goals>
                                    <goal>%s</goal>
                                </goals>
                                <configuration>
                                    <arguments>run build</arguments>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>""".formatted(installGoal, installVersionConfig, runGoal, runGoal, runGoal, runGoal));
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
                
                # Excludes for npm/pnpm
                src/main/frontend/node_modules
                src/main/resources/css/application.css
                """;
        if (!Files.exists(path)) {
            Files.writeString(path, s, StandardOpenOption.CREATE);
        } else {
            Files.writeString(path, s, StandardOpenOption.APPEND);
        }
    }

    @Override
    public boolean isTailwindVersionOf(TailwindVersion tailwindVersion, Class<? extends LiveReloadInitService> liveReloadInitServiceClass) {
        return tailwindVersion.equals(TailwindVersion.VERSION_3)
                && liveReloadInitServiceClass.isAssignableFrom(DevToolsBasedLiveReloadInitService.class);
    }
}
