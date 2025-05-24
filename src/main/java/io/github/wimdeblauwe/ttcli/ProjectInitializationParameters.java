package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.SpringBootProjectCreationParameters;
import io.github.wimdeblauwe.ttcli.deps.TailwindCssWebDependency;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceParameters;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindDependency;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindVersion;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import jakarta.annotation.Nullable;

import java.nio.file.Path;
import java.util.List;

public record ProjectInitializationParameters(Path basePath,
                                              SpringBootProjectCreationParameters springBootProjectCreationParameters,
                                              LiveReloadInitServiceParameters liveReloadInitServiceParameters,
                                              List<WebDependency> webDependencies,
                                              @Nullable TailwindVersion tailwindVersion,
                                              List<TailwindDependency> tailwindDependencies,
                                              TemplateEngineType templateEngineType) {
    public String projectName() {
        return springBootProjectCreationParameters().projectName();
    }

    public boolean hasTailwindCssWebDependency() {
        return webDependencies.stream().anyMatch(webDependency -> webDependency instanceof TailwindCssWebDependency);
    }
}
