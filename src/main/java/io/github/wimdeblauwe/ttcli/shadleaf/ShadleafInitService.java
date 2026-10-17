package io.github.wimdeblauwe.ttcli.shadleaf;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.help.HelpTextInitService;
import io.github.wimdeblauwe.ttcli.util.PropertiesFilesUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Configures Shadleaf in the generated project.
 * See <a href="https://shadleaf.dev/current/getting-started/">https://shadleaf.dev/current/getting-started/</a>
 */
@Component
public class ShadleafInitService {

    private final HelpTextInitService helpTextInitService;

    public ShadleafInitService(HelpTextInitService helpTextInitService) {
        this.helpTextInitService = helpTextInitService;
    }

    public void generate(ProjectInitializationParameters parameters) throws IOException {
        if (!parameters.hasShadleafWebDependency()) {
            return;
        }

        Path basePath = parameters.basePath();
        if (parameters.hasTailwindCssWebDependency()) {
            // The application compiles Tailwind itself, so Shadleaf should not ship Tailwind's reset and theme variables
            PropertiesFilesUtil.writeOrUpdatePropertiesFile(basePath,
                    "application.properties",
                    """

                            shadleaf.assets.variant=embedded
                            """);
        }

        helpTextInitService.addHelpText(basePath, helpText(parameters.hasTailwindCssWebDependency()));
    }

    static String helpText(boolean hasTailwindCssWebDependency) {
        String helpText = """

                ## Shadleaf

                The project uses [Shadleaf](https://shadleaf.dev) components. The `sl/layout` fragments in
                `layout/main.html` include the theme script and Shadleaf's CSS and JavaScript.
                See https://shadleaf.dev/current/components/ for all available components.

                Shadleaf also loads Alpine.js, so you can use Alpine in your own markup without adding it yourself.
                To register your own Alpine components, listen for `alpine:init` in a script that is placed
                *before* the `sl/layout :: assets` fragment. See https://shadleaf.dev/current/guides/alpine/
                """;
        if (hasTailwindCssWebDependency) {
            helpText += """

                    Because the project compiles Tailwind CSS itself, `shadleaf.assets.variant=embedded` is set in
                    `application.properties`, and `application.css` maps Shadleaf's tokens onto Tailwind utilities
                    (e.g. `bg-primary`, `text-muted-foreground`). See https://shadleaf.dev/current/guides/tailwind/
                    """;
        }
        return helpText;
    }
}
