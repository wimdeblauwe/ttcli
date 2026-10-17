package io.github.wimdeblauwe.ttcli.shadleaf;

import io.github.wimdeblauwe.ttcli.deps.ShadleafWebDependency;
import io.github.wimdeblauwe.ttcli.deps.TailwindCssWebDependency;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import io.github.wimdeblauwe.ttcli.help.HelpTextInitService;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.github.wimdeblauwe.ttcli.TestParameters.projectInitializationParameters;
import static org.assertj.core.api.Assertions.assertThat;

class ShadleafInitServiceTest {

    @TempDir
    Path tempDir;

    private final ShadleafInitService shadleafInitService = new ShadleafInitService(new HelpTextInitService());

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(tempDir.resolve("src/main/resources"));
        Files.writeString(tempDir.resolve("src/main/resources/application.properties"), "spring.application.name=demo\n");
        Files.writeString(tempDir.resolve("HELP.md"), "# Getting Started\n");
    }

    @Test
    void testGenerate_withoutShadleaf() throws IOException {
        generate(List.of(new TailwindCssWebDependency()));

        assertThat(applicationProperties()).doesNotContain("shadleaf");
        assertThat(helpText()).doesNotContain("Shadleaf");
    }

    @Test
    void testGenerate_shadleafOnly() throws IOException {
        generate(List.of(new ShadleafWebDependency()));

        assertThat(applicationProperties()).doesNotContain("shadleaf");
        assertThat(helpText()).contains("## Shadleaf")
                .contains("alpine:init")
                .doesNotContain("shadleaf.assets.variant=embedded");
    }

    @Test
    void testGenerate_shadleafWithTailwind() throws IOException {
        generate(List.of(new ShadleafWebDependency(), new TailwindCssWebDependency()));

        assertThat(applicationProperties()).contains("shadleaf.assets.variant=embedded")
                .doesNotContain("shadleaf.assets.alpine");
        assertThat(helpText()).contains("## Shadleaf")
                .contains("shadleaf.assets.variant=embedded");
    }

    private void generate(List<WebDependency> webDependencies) throws IOException {
        shadleafInitService.generate(projectInitializationParameters(tempDir, "npm-based", webDependencies, new TemplateEngineType.Thymeleaf(true)));
    }

    private String applicationProperties() throws IOException {
        return Files.readString(tempDir.resolve("src/main/resources/application.properties"));
    }

    private String helpText() throws IOException {
        return Files.readString(tempDir.resolve("HELP.md"));
    }
}
