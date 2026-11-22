package io.github.wimdeblauwe.ttcli.thymeleaf;

import io.github.wimdeblauwe.ttcli.deps.AlpineWebDependency;
import io.github.wimdeblauwe.ttcli.deps.HtmxVersion;
import io.github.wimdeblauwe.ttcli.deps.HtmxWebDependency;
import io.github.wimdeblauwe.ttcli.deps.ShadleafWebDependency;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.github.wimdeblauwe.ttcli.TestParameters.projectInitializationParameters;
import static org.assertj.core.api.Assertions.assertThat;

class ThymeleafTemplatesInitServiceTest {

    @TempDir
    Path tempDir;

    private final ThymeleafTemplatesInitService thymeleafTemplatesInitService = new ThymeleafTemplatesInitService();

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGenerate_withShadleaf(boolean useLayoutDialect) throws IOException {
        String layout = generateLayout("npm-based", List.of(new ShadleafWebDependency()), useLayoutDialect);

        assertThat(layout).contains("xmlns:sl=\"https://shadleaf.dev/sl\"")
                .contains("<sl:theme-toggle></sl:theme-toggle>")
                .contains("<sl:button variant=\"outline\">Example button</sl:button>")
                .doesNotContain("REPLACE WITH");
        int themeScriptIndex = layout.indexOf("~{sl/layout :: theme-script}");
        int assetsIndex = layout.indexOf("~{sl/layout :: assets}");
        int applicationCssIndex = layout.indexOf("@{/css/application.css}");
        assertThat(themeScriptIndex).isPositive();
        assertThat(themeScriptIndex).isLessThan(assetsIndex);
        assertThat(assetsIndex).isLessThan(applicationCssIndex);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGenerate_withShadleafAndHtmx4(boolean useLayoutDialect) throws IOException {
        String layout = generateLayout("npm-based", List.of(new HtmxWebDependency(), new ShadleafWebDependency()), useLayoutDialect);

        int themeScriptIndex = layout.indexOf("~{sl/layout :: theme-script}");
        int htmxIndex = layout.indexOf("@{/webjars/htmx.org/dist/htmx.min.js}");
        int alpineCompatIndex = layout.indexOf("@{/webjars/htmx.org/dist/ext/hx-alpine-compat.min.js}");
        int assetsIndex = layout.indexOf("~{sl/layout :: assets}");
        assertThat(themeScriptIndex).isPositive();
        assertThat(themeScriptIndex).isLessThan(htmxIndex);
        assertThat(htmxIndex).isLessThan(alpineCompatIndex);
        assertThat(alpineCompatIndex).isLessThan(assetsIndex);
        assertThat(layout.indexOf("htmx.min.js")).isEqualTo(layout.lastIndexOf("htmx.min.js"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGenerate_withShadleafAndHtmx2(boolean useLayoutDialect) throws IOException {
        String layout = generateLayout("npm-based", List.of(new HtmxWebDependency().withVersion(HtmxVersion.VERSION_2), new ShadleafWebDependency()), useLayoutDialect);

        assertThat(layout).doesNotContain("hx-alpine-compat");
        assertThat(layout.indexOf("@{/webjars/htmx.org/dist/htmx.min.js}")).isPositive()
                .isLessThan(layout.indexOf("~{sl/layout :: assets}"));
        assertThat(layout.indexOf("htmx.min.js")).isEqualTo(layout.lastIndexOf("htmx.min.js"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGenerate_withHtmx4WithoutShadleaf(boolean useLayoutDialect) throws IOException {
        String layout = generateLayout("npm-based", List.of(new HtmxWebDependency()), useLayoutDialect);

        assertThat(layout).contains("@{/webjars/htmx.org/dist/htmx.min.js}")
                .doesNotContain("hx-alpine-compat")
                .doesNotContain("REPLACE WITH");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGenerate_withShadleafAndVite(boolean useLayoutDialect) throws IOException {
        String layout = generateLayout("vite", List.of(new ShadleafWebDependency()), useLayoutDialect);

        assertThat(layout.indexOf("~{sl/layout :: assets}")).isPositive()
                .isLessThan(layout.indexOf("<vite:client>"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGenerate_withoutShadleaf(boolean useLayoutDialect) throws IOException {
        String layout = generateLayout("npm-based", List.of(new AlpineWebDependency()), useLayoutDialect);

        assertThat(layout).doesNotContain("sl:")
                .doesNotContain("sl/layout")
                .doesNotContain("REPLACE WITH")
                .contains("/webjars/alpinejs/dist/cdn.min.js");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGenerate_withAlpineAndHtmx4(boolean useLayoutDialect) throws IOException {
        String layout = generateLayout("npm-based", List.of(new AlpineWebDependency(), new HtmxWebDependency()), useLayoutDialect);

        int htmxIndex = layout.indexOf("@{/webjars/htmx.org/dist/htmx.min.js}");
        int alpineCompatIndex = layout.indexOf("@{/webjars/htmx.org/dist/ext/hx-alpine-compat.min.js}");
        int alpineIndex = layout.indexOf("@{/webjars/alpinejs/dist/cdn.min.js}");
        assertThat(htmxIndex).isPositive();
        assertThat(htmxIndex).isLessThan(alpineCompatIndex);
        assertThat(alpineCompatIndex).isLessThan(alpineIndex);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGenerate_withAlpineAndHtmx2(boolean useLayoutDialect) throws IOException {
        String layout = generateLayout("npm-based", List.of(new AlpineWebDependency(), new HtmxWebDependency().withVersion(HtmxVersion.VERSION_2)), useLayoutDialect);

        assertThat(layout).contains("@{/webjars/htmx.org/dist/htmx.min.js}")
                .contains("@{/webjars/alpinejs/dist/cdn.min.js}")
                .doesNotContain("hx-alpine-compat");
    }

    private String generateLayout(String liveReloadId,
                                  List<WebDependency> webDependencies,
                                  boolean useLayoutDialect) throws IOException {
        thymeleafTemplatesInitService.generate(projectInitializationParameters(tempDir, liveReloadId, webDependencies, new TemplateEngineType.Thymeleaf(useLayoutDialect)));
        return Files.readString(tempDir.resolve("src/main/resources/templates/layout/main.html"));
    }
}
