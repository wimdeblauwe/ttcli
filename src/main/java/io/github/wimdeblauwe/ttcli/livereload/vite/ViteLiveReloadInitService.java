package io.github.wimdeblauwe.ttcli.livereload.vite;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceException;
import io.github.wimdeblauwe.ttcli.livereload.helper.NpmHelper;
import io.github.wimdeblauwe.ttcli.maven.MavenPomReaderWriter;
import io.github.wimdeblauwe.ttcli.npm.InstalledApplicationVersions;
import io.github.wimdeblauwe.ttcli.npm.NodeService;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import io.github.wimdeblauwe.ttcli.util.PropertiesFilesUtil;
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
@Order(5)
public class ViteLiveReloadInitService implements LiveReloadInitService {
    private static final String VITE_SPRING_BOOT_VERSION = "0.9.0";

    protected final NodeService nodeService;

    public ViteLiveReloadInitService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public String getId() {
        return "vite";
    }

    @Override
    public String getName() {
        return "Vite";
    }

    @Override
    public String getHelpText() {
        return """
                # Live reload setup

                This project uses Vite to have live reloading.

                Use the following steps to get it working:

                1. Start the Vite development server with `npm run dev`.
                2. Run the Spring Boot application with the `local` profile. You can do this from your IDE,
                or via the command line using `mvn spring-boot:run -Dspring-boot.run.profiles=local`.
                3. Open your browser at http://localhost:8080

                You should now be able to change any HTML or CSS and have the browser reload upon saving the file.

                PS: It is also possible to use the URL that Vite uses (Usually http://localhost:5173) given the
                Spring Boot application runs on port 8080. If another port is used, you will need to edit `vite.config.js`.
                """;

    }

    @Override
    public Set<String> additionalSpringInitializrDependencies() {
        return Set.of();
    }

    @Override
    public void generate(ProjectInitializationParameters projectInitializationParameters) throws LiveReloadInitServiceException {
        try {
            InstalledApplicationVersions installedApplicationVersions = nodeService.checkIfNodeAndNpmAreInstalled();
            Path basePath = projectInitializationParameters.basePath();
            nodeService.createPackageJsonForModule(basePath,
                    projectInitializationParameters.projectName());
            nodeService.installNpmDevDependencies(basePath,
                    npmDevDependencies());
            nodeService.insertPackageJsonScripts(basePath, npmScripts());

            createViteConfig(basePath);

            MavenPomReaderWriter mavenPomReaderWriter = MavenPomReaderWriter.readFrom(basePath);
            if (projectInitializationParameters.templateEngineType() == TemplateEngineType.THYMELEAF) {
                mavenPomReaderWriter.addDependency("io.github.wimdeblauwe", "vite-spring-boot-thymeleaf", VITE_SPRING_BOOT_VERSION);
            } else if (projectInitializationParameters.templateEngineType() == TemplateEngineType.JTE) {
                mavenPomReaderWriter.addDependency("io.github.wimdeblauwe", "vite-spring-boot-jte", VITE_SPRING_BOOT_VERSION);
            }
            mavenPomReaderWriter.write();

            NpmHelper.updateMavenPom(mavenPomReaderWriter, installedApplicationVersions, false);
            NpmHelper.updateGitIgnore(basePath);

            updateSpringApplicationProperties(basePath, projectInitializationParameters.templateEngineType());
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
        return null;
    }

    @Override
    public boolean isApplicableForTemplateEngine(TemplateEngineType templateEngineType) {
        return templateEngineType.equals(TemplateEngineType.THYMELEAF)
                || templateEngineType.equals(TemplateEngineType.JTE);
    }

    protected void createViteConfig(Path basePath) throws IOException {
        Path path = basePath.resolve("vite.config.js");
        String content = """
                import {defineConfig} from 'vite';
                import path from 'path';
                import springBoot from '@wim.deblauwe/vite-plugin-spring-boot';
                
                export default defineConfig({
                    plugins: [
                        springBoot()
                    ],
                    root: path.join(__dirname, './src/main/resources'),
                    build: {
                        manifest: true,
                        rollupOptions: {
                            input: [
                                '/static/css/application.css'
                            ]
                        },
                        outDir: path.join(__dirname, `./target/classes/static`),
                        copyPublicDir: false,
                        emptyOutDir: true
                    },
                    server: {
                        proxy: {
                            // Proxy all backend requests to Spring Boot except for static assets
                            '^/(?!static|assets|@|.*\\\\.(js|css|png|svg|jpg|jpeg|gif|ico|woff|woff2)$)': {
                                target: 'http://localhost:8080',  // Proxy to Spring Boot backend
                                changeOrigin: true,
                                secure: false
                            }
                        },
                        watch: {
                            ignored: ['target/**']
                        }
                    }
                });
                """;
        Files.writeString(path, content, StandardOpenOption.CREATE);
    }

    private void updateSpringApplicationProperties(Path base, TemplateEngineType templateEngineType) throws IOException {
        if (templateEngineType.equals(TemplateEngineType.THYMELEAF)) {
            PropertiesFilesUtil.writeOrUpdatePropertiesFile(base,
                    "application-local.properties",
                    """
                            spring.thymeleaf.cache=false
                            spring.web.resources.chain.cache=false
                            
                            vite.mode=dev
                            """);
            PropertiesFilesUtil.writeOrUpdatePropertiesFile(base,
                    "application.properties",
                    """                            
                            vite.mode=build
                            """);
        } else if (templateEngineType.equals(TemplateEngineType.JTE)) {
            PropertiesFilesUtil.writeOrUpdatePropertiesFile(base,
                    "application-local.properties",
                    """
                            gg.jte.usePrecompiledTemplates=false
                            gg.jte.development-mode=true
                            spring.web.resources.chain.cache=false
                            
                            vite.mode=dev
                            """);
            PropertiesFilesUtil.removePropertyFromPropertiesFile(base, "application.properties", "gg.jte.development-mode");
            PropertiesFilesUtil.writeOrUpdatePropertiesFile(base,
                    "application.properties",
                    """
                            gg.jte.usePrecompiledTemplates=true
                            
                            vite.mode=build
                            """);
        }
    }


    protected List<String> npmDevDependencies() {
        return List.of("vite", "@wim.deblauwe/vite-plugin-spring-boot");
    }

    protected LinkedHashMap<String, String> npmScripts() {
        LinkedHashMap<String, String> scripts = new LinkedHashMap<>();
        scripts.put("dev", "vite");
        scripts.put("dev-open", "vite --open http://localhost:8080");
        scripts.put("build", "vite build");
        return scripts;
    }
}
