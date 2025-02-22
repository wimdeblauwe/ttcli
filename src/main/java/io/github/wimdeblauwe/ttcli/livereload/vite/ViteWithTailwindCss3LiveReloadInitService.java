package io.github.wimdeblauwe.ttcli.livereload.vite;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceException;
import io.github.wimdeblauwe.ttcli.livereload.TailwindCssSpecializedLiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.helper.TailwindCss3Helper;
import io.github.wimdeblauwe.ttcli.npm.NodeService;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindVersion;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Component
public class ViteWithTailwindCss3LiveReloadInitService extends ViteLiveReloadInitService implements TailwindCssSpecializedLiveReloadInitService {
    public ViteWithTailwindCss3LiveReloadInitService(NodeService nodeService) {
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

            TailwindCss3Helper.createApplicationCss(basePath,
                    "src/main/resources/static/css/application.css");
            TailwindCss3Helper.setupTailwindConfig(basePath, "./src/main/resources/templates/**/*.html");
            // Generate the postcss.config.js file for Tailwind CSS
            nodeService.runNpxCommand(basePath, List.of("tailwindcss@3", "init", "-p"));

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
                        Stream.of("tailwindcss@3", "postcss", "autoprefixer"))
                .toList();
    }

    @Override
    public boolean isTailwindVersionOf(TailwindVersion tailwindVersion, Class<? extends LiveReloadInitService> liveReloadInitServiceClass) {
        return tailwindVersion.equals(TailwindVersion.VERSION_3)
                && liveReloadInitServiceClass.isAssignableFrom(ViteLiveReloadInitService.class);
    }
}
