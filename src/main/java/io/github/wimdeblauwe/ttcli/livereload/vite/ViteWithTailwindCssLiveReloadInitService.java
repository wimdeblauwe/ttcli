package io.github.wimdeblauwe.ttcli.livereload.vite;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceException;
import io.github.wimdeblauwe.ttcli.livereload.TailwindCssSpecializedLiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.helper.TailwindCssHelper;
import io.github.wimdeblauwe.ttcli.npm.NodeService;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindVersion;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

@Component
public class ViteWithTailwindCssLiveReloadInitService extends ViteLiveReloadInitService implements TailwindCssSpecializedLiveReloadInitService {
    public ViteWithTailwindCssLiveReloadInitService(NodeService nodeService) {
        super(nodeService);
    }

    @Override
    public String getId() {
        return "vite-with-tailwind-css";
    }

    @Override
    public String getName() {
        return "Vite with Tailwind CSS";
    }

    @Override
    public void generate(ProjectInitializationParameters projectInitializationParameters) throws LiveReloadInitServiceException {
        super.generate(projectInitializationParameters);

        try {
            Path basePath = projectInitializationParameters.basePath();

            TailwindCssHelper.createApplicationCss(basePath,
                    "src/main/resources/static/css/application.css",
                    "../../templates");
        } catch (IOException e) {
            throw new LiveReloadInitServiceException(e);
        }
    }

    @Override
    public Path getTailwindConfigFileParentPath(ProjectInitializationParameters parameters) {
        return parameters.basePath();
    }

    @Override
    protected List<String> npmDevDependencies() {
        return Stream.concat(super.npmDevDependencies().stream(),
                        Stream.of("tailwindcss", "@tailwindcss/vite"))
                .toList();
    }

    protected void createViteConfig(Path basePath, TemplateEngineType templateEngineType) throws IOException {
        Path path = basePath.resolve("vite.config.js");
        String content = switch (templateEngineType) {
            case THYMELEAF -> viteConfigForThymeleaf();
            case JTE -> viteConfigForJte();
        };
        Files.writeString(path, content, StandardOpenOption.CREATE);
    }

    private static String viteConfigForThymeleaf() {
        return """
                import {defineConfig} from 'vite';
                import tailwindcss from '@tailwindcss/vite';
                import path from 'path';
                import springBoot from '@wim.deblauwe/vite-plugin-spring-boot';
                
                export default defineConfig({
                    plugins: [
                        tailwindcss(),
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
    }

    private static String viteConfigForJte() {
        return """
                import {defineConfig} from 'vite';
                import tailwindcss from '@tailwindcss/vite';
                import path from 'path';
                import springBoot from '@wim.deblauwe/vite-plugin-spring-boot';
                
                export default defineConfig({
                    plugins: [
                        tailwindcss(),
                        springBoot({
                                        fullCopyFilePaths: {
                                            include: ['jte/**/*.jte'],
                                        }
                        })
                    ],
                    root: path.join(__dirname, './src/main/'),
                    build: {
                        manifest: true,
                        rollupOptions: {
                            input: [
                                '/resources/static/css/application.css'
                            ]
                        },
                        outDir: path.join(__dirname, `./target/classes/static`),
                        copyPublicDir: false,
                        emptyOutDir: true
                    },
                    server: {
                        proxy: {
                            // Proxy all backend requests to Spring Boot except for static assets
                            '^/(?!resources/static|assets|@|.*\\\\.(js|css|png|svg|jpg|jpeg|gif|ico|woff|woff2)$)': {
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
    }

    @Override
    public boolean isTailwindVersionOf(TailwindVersion tailwindVersion, Class<? extends LiveReloadInitService> liveReloadInitServiceClass) {
        return tailwindVersion.equals(TailwindVersion.VERSION_4)
                && liveReloadInitServiceClass.isAssignableFrom(ViteLiveReloadInitService.class);
    }
}
