package io.github.wimdeblauwe.ttcli;

import jakarta.annotation.Nullable;

/**
 * The raw (unvalidated) values of the {@code init} command-line options. A {@code null} value means the option
 * was not given, so the question is asked (or defaulted when running non-interactively).
 */
public record InitOptions(@Nullable String group,
                          @Nullable String artifact,
                          @Nullable String name,
                          @Nullable String springBootVersion,
                          @Nullable String javaVersion,
                          @Nullable String templateEngine,
                          @Nullable String thymeleafLayoutDialect,
                          @Nullable String liveReload,
                          @Nullable String packageManager,
                          @Nullable String webDeps,
                          @Nullable String htmxVersion,
                          @Nullable String tailwindVersion,
                          @Nullable String tailwindDeps,
                          boolean nonInteractive) {

    public InitOptions {
        group = blankToNull(group);
        artifact = blankToNull(artifact);
        name = blankToNull(name);
        springBootVersion = blankToNull(springBootVersion);
        javaVersion = blankToNull(javaVersion);
        templateEngine = blankToNull(templateEngine);
        thymeleafLayoutDialect = blankToNull(thymeleafLayoutDialect);
        liveReload = blankToNull(liveReload);
        packageManager = blankToNull(packageManager);
        webDeps = blankToNull(webDeps);
        htmxVersion = blankToNull(htmxVersion);
        tailwindVersion = blankToNull(tailwindVersion);
        tailwindDeps = blankToNull(tailwindDeps);
    }

    @Nullable
    private static String blankToNull(@Nullable String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
