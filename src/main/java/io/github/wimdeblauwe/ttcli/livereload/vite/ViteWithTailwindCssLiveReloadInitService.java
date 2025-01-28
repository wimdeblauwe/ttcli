package io.github.wimdeblauwe.ttcli.livereload.vite;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceException;
import io.github.wimdeblauwe.ttcli.livereload.TailwindCssSpecializedLiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.helper.TailwindCssHelper;
import io.github.wimdeblauwe.ttcli.npm.NodeService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(6)
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
                                                   "src/main/resources/static/css/application.css");
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

    @Override
    protected void createViteConfig(Path basePath) throws IOException {
        Path path = basePath.resolve("vite.config.js");
        String content = """
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
        Files.writeString(path, content, StandardOpenOption.CREATE);
    }

    @Override
    public boolean isTailwindVersionOf(Class<? extends LiveReloadInitService> liveReloadInitServiceClass) {
        return liveReloadInitServiceClass.isAssignableFrom(ViteLiveReloadInitService.class);
    }
}
