package io.github.wimdeblauwe.ttcli.jte;

import io.github.wimdeblauwe.ttcli.deps.AlpineWebDependency;
import io.github.wimdeblauwe.ttcli.deps.HtmxVersion;
import io.github.wimdeblauwe.ttcli.deps.HtmxWebDependency;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.github.wimdeblauwe.ttcli.TestParameters.projectInitializationParameters;
import static org.assertj.core.api.Assertions.assertThat;

class JteTemplatesInitServiceTest {

    @TempDir
    Path tempDir;

    private final JteTemplatesInitService jteTemplatesInitService = new JteTemplatesInitService();

    @Test
    void testGenerate_withAlpineAndHtmx4() throws IOException {
        String layout = generateLayout(List.of(new AlpineWebDependency(), new HtmxWebDependency()));

        int htmxIndex = layout.indexOf("/webjars/htmx.org/dist/htmx.min.js");
        int alpineCompatIndex = layout.indexOf("/webjars/htmx.org/dist/ext/hx-alpine-compat.min.js");
        int alpineIndex = layout.indexOf("/webjars/alpinejs/dist/cdn.min.js");
        assertThat(htmxIndex).isPositive();
        assertThat(htmxIndex).isLessThan(alpineCompatIndex);
        assertThat(alpineCompatIndex).isLessThan(alpineIndex);
        assertThat(layout).doesNotContain("REPLACE WITH");
    }

    @Test
    void testGenerate_withAlpineAndHtmx2() throws IOException {
        String layout = generateLayout(List.of(new AlpineWebDependency(), new HtmxWebDependency().withVersion(HtmxVersion.VERSION_2)));

        assertThat(layout).contains("/webjars/htmx.org/dist/htmx.min.js")
                .contains("/webjars/alpinejs/dist/cdn.min.js")
                .doesNotContain("hx-alpine-compat");
    }

    @Test
    void testGenerate_withHtmx4WithoutAlpine() throws IOException {
        String layout = generateLayout(List.of(new HtmxWebDependency()));

        assertThat(layout).contains("/webjars/htmx.org/dist/htmx.min.js")
                .doesNotContain("hx-alpine-compat");
    }

    private String generateLayout(List<WebDependency> webDependencies) throws IOException {
        jteTemplatesInitService.generate(projectInitializationParameters(tempDir, "npm-based", webDependencies, new TemplateEngineType.Jte()));
        return Files.readString(tempDir.resolve("src/main/jte/layout/main.jte"));
    }
}
