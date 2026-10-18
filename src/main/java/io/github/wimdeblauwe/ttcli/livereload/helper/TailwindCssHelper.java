package io.github.wimdeblauwe.ttcli.livereload.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TailwindCssHelper {

    public static void createApplicationCss(Path base,
                                            String cssFilePath,
                                            String sourcePath,
                                            boolean includeShadleafTokens) throws IOException {
        Path path = base.resolve(cssFilePath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, applicationCssContent(sourcePath, includeShadleafTokens));
    }

    static String applicationCssContent(String sourcePath,
                                        boolean includeShadleafTokens) {
        String content = """
                @import "tailwindcss";
                @source "%s";
                """.formatted(sourcePath);
        if (includeShadleafTokens) {
            content += shadleafTokensContent();
        }
        return content;
    }

    /**
     * See <a href="https://shadleaf.dev/current/guides/tailwind/">https://shadleaf.dev/current/guides/tailwind/</a>
     */
    private static String shadleafTokensContent() {
        return """

                /* Make `dark:` follow the Shadleaf theme toggle instead of the operating system */
                @custom-variant dark (&:where(.dark, .dark *));

                /* Utilities on Shadleaf's tokens (e.g. `bg-primary`, `text-muted-foreground`) */
                @theme inline {
                  --radius-sm: calc(var(--radius) - 4px);
                  --radius-md: calc(var(--radius) - 2px);
                  --radius-lg: var(--radius);
                  --radius-xl: calc(var(--radius) + 4px);
                  --color-background: var(--background);
                  --color-foreground: var(--foreground);
                  --color-card: var(--card);
                  --color-card-foreground: var(--card-foreground);
                  --color-popover: var(--popover);
                  --color-popover-foreground: var(--popover-foreground);
                  --color-primary: var(--primary);
                  --color-primary-foreground: var(--primary-foreground);
                  --color-secondary: var(--secondary);
                  --color-secondary-foreground: var(--secondary-foreground);
                  --color-muted: var(--muted);
                  --color-muted-foreground: var(--muted-foreground);
                  --color-accent: var(--accent);
                  --color-accent-foreground: var(--accent-foreground);
                  --color-destructive: var(--destructive);
                  --color-border: var(--border);
                  --color-input: var(--input);
                  --color-ring: var(--ring);
                }

                /* Page background and borders in the colours of the Shadleaf components */
                @layer base {
                  body {
                    background-color: var(--background);
                    color: var(--foreground);
                  }

                  * {
                    border-color: var(--border);
                  }
                }
                """;
    }

    private TailwindCssHelper() {
    }
}
