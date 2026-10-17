package io.github.wimdeblauwe.ttcli.tailwind;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TailwindDependencyInitServiceTest {

    @TempDir
    Path tempDir;

    private final TailwindDependencyInitService service = new TailwindDependencyInitService(null);

    @Test
    void testUpdateTailwindConfigFile_thymeleaf() throws IOException {
        String css = updateCss(new TemplateEngineType.Thymeleaf(true));

        assertThat(css).contains("@source \"../../templates\";");
        assertThat(css).doesNotContain("jte");
        assertThat(css).contains("@plugin \"@tailwindcss/forms\";");
    }

    @Test
    void testUpdateTailwindConfigFile_jte() throws IOException {
        String css = updateCss(new TemplateEngineType.Jte());

        assertThat(css).contains("@source \"../../../jte\";");
        assertThat(css).doesNotContain("\"../../templates\"");
        assertThat(css).contains("@plugin \"@tailwindcss/forms\";");
    }

    private String updateCss(TemplateEngineType templateEngineType) throws IOException {
        Path cssFile = tempDir.resolve("src/main/resources/static/css/application.css");
        Files.createDirectories(cssFile.getParent());
        Files.writeString(cssFile, "@import \"tailwindcss\";\n@source \"../../templates\";\n");

        ProjectInitializationParameters parameters = new ProjectInitializationParameters(tempDir,
                                                                                         null,
                                                                                         null,
                                                                                         List.of(),
                                                                                         TailwindVersion.VERSION_4,
                                                                                         List.of(new FormsTailwindDependency()),
                                                                                         templateEngineType);
        service.updateTailwindConfigFile(tempDir, parameters);
        return Files.readString(cssFile);
    }
}
