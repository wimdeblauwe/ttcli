package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.SpringBootProjectCreationParameters;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindDependency;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceParameters;

import java.nio.file.Path;
import java.util.List;

public record ProjectInitializationParameters(Path basePath,
                                              SpringBootProjectCreationParameters springBootProjectCreationParameters,
                                              LiveReloadInitServiceParameters liveReloadInitServiceParameters,
                                              List<WebDependency> webDependencies,
                                              List<TailwindDependency> tailwindDependencies) {
    public String projectName() {
        return springBootProjectCreationParameters().projectName();
    }
}
