package io.github.wimdeblauwe.ttcli.tailwind;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceException;
import io.github.wimdeblauwe.ttcli.npm.NodeService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TailwindDependencyInitService {
    private final NodeService nodeService;

    public TailwindDependencyInitService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void generate(LiveReloadInitService liveReloadInitService,
                         ProjectInitializationParameters parameters) {
        try {
            List<TailwindDependency> tailwindDependencies = parameters.tailwindDependencies();
            if (!tailwindDependencies.isEmpty()) {
                List<String> npmPackages = tailwindDependencies.stream().map(TailwindDependency::npmPackageName)
                                                               .toList();
                Path basePath = liveReloadInitService.getTailwindConfigFileParentPath(parameters);
                nodeService.installNpmDevDependencies(basePath, npmPackages);

                updateTailwindConfigFile(basePath, parameters);
            }
        } catch (IOException e) {
            throw new LiveReloadInitServiceException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LiveReloadInitServiceException(e);
        }
    }

    private void updateTailwindConfigFile(Path basePath,
                                          ProjectInitializationParameters parameters) throws IOException {
        Path tailwindConfigFilePath = basePath.resolve("tailwind.config.js");
        byte[] bytes = Files.readAllBytes(tailwindConfigFilePath);
        String s = new String(bytes);
        s = s.replaceFirst("plugins: \\[]", "plugins: [" + parameters.tailwindDependencies()
                                                                     .stream()
                                                                     .map(it -> String.format("require('%s')", it.pluginName()))
                                                                     .collect(Collectors.joining(",")) + "]");
        Files.writeString(tailwindConfigFilePath, s);
    }
}
