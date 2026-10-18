package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.SpringBootProjectCreationParameters;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceParameters;
import io.github.wimdeblauwe.ttcli.npm.PackageManager;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindVersion;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;

import java.nio.file.Path;
import java.util.List;

public final class TestParameters {

    public static ProjectInitializationParameters projectInitializationParameters(Path basePath,
                                                                                  String liveReloadId,
                                                                                  List<WebDependency> webDependencies,
                                                                                  TemplateEngineType templateEngineType) {
        return new ProjectInitializationParameters(basePath,
                new SpringBootProjectCreationParameters("com.example", "demo", "Demo", "4.1.0", "25"),
                new LiveReloadInitServiceParameters(liveReloadId, PackageManager.NPM),
                webDependencies,
                TailwindVersion.VERSION_4,
                List.of(),
                templateEngineType);
    }

    private TestParameters() {
    }
}
