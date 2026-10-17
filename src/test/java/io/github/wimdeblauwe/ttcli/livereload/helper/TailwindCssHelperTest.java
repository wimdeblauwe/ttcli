package io.github.wimdeblauwe.ttcli.livereload.helper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TailwindCssHelperTest {

    @Test
    void testApplicationCssContent_withoutShadleaf() {
        String content = TailwindCssHelper.applicationCssContent("../../templates", false);

        assertThat(content).isEqualToNormalizingNewlines("""
                @import "tailwindcss";
                @source "../../templates";
                """);
    }

    @Test
    void testApplicationCssContent_withShadleaf() {
        String content = TailwindCssHelper.applicationCssContent("../../templates", true);

        assertThat(content).startsWith("""
                @import "tailwindcss";
                @source "../../templates";
                """);
        assertThat(content).contains("@custom-variant dark (&:where(.dark, .dark *));");
        assertThat(content).contains("@theme inline {");
        assertThat(content).contains("--color-primary: var(--primary);");
        assertThat(content).contains("background-color: var(--background);");
        assertThat(content).contains("border-color: var(--border);");
    }
}
