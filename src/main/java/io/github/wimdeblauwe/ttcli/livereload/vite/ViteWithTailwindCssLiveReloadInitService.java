package io.github.wimdeblauwe.ttcli.livereload.vite;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceException;
import io.github.wimdeblauwe.ttcli.livereload.TailwindCssSpecializedLiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.helper.TailwindCssHelper;
import io.github.wimdeblauwe.ttcli.npm.NodeService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

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
            TailwindCssHelper.setupTailwindConfig(basePath, "./src/main/resources/templates/**/*.html");
            // Generate the postcss.config.js file for Tailwind CSS
            nodeService.runNpxCommand(basePath, List.of("tailwindcss", "init", "-p"));

        } catch (IOException e) {
            throw new LiveReloadInitServiceException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
                             Stream.of("tailwindcss", "postcss", "autoprefixer"))
                     .toList();
    }

    @Override
    public boolean isTailwindVersionOf(Class<? extends LiveReloadInitService> liveReloadInitServiceClass) {
        return liveReloadInitServiceClass.isAssignableFrom(ViteLiveReloadInitService.class);
    }
}
