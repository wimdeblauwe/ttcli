package io.github.wimdeblauwe.ttcli.thymeleaf;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Component
public class ThymeleafTemplatesInitService {

    public void generate(ProjectInitializationParameters parameters) throws IOException {
        Path basePath = parameters.basePath();
        createDefaultLayoutTemplate(basePath, parameters.webDependencies());
        createDefaultIndexTemplate(basePath);
        createDefaultApplicationCss(basePath);
    }

    private void createDefaultLayoutTemplate(Path base,
                                             List<WebDependency> webDependencies) throws IOException {
        Path layoutTemplate = base.resolve("src/main/resources/templates/layout/main.html");
        Files.createDirectories(layoutTemplate.getParent());
        String source = "/files/templates/layout/main.html";
        try (InputStream stream = getClass().getResourceAsStream(source)) {
            Files.copy(Objects.requireNonNull(stream, () -> "Could not find " + source),
                       layoutTemplate);
        }

        StringBuilder cssLinksForLayoutTemplate = new StringBuilder();
        for (WebDependency webDependency : webDependencies) {
            String cssForDependency = webDependency.getCssLinksForLayoutTemplate();
            if (cssForDependency != null) {
                cssLinksForLayoutTemplate
                        .append('\n')
                        .append(cssForDependency);
            }
        }
        insertCssLinksToLayoutTemplate(layoutTemplate, cssLinksForLayoutTemplate.toString());

        StringBuilder jsLinksForLayoutTemplate = new StringBuilder();
        for (WebDependency webDependency : webDependencies) {
            String jsForDependency = webDependency.getJsLinksForLayoutTemplate();
            if (jsForDependency != null) {
                jsLinksForLayoutTemplate
                        .append('\n')
                        .append(jsForDependency);
            }
        }
        insertJsLinksToLayoutTemplate(layoutTemplate, jsLinksForLayoutTemplate.toString());
    }

    private void insertCssLinksToLayoutTemplate(Path layoutTemplate,
                                                String cssLinksForLayoutTemplate) throws IOException {
        String layoutTemplateContent = Files.readString(layoutTemplate);
        String result = layoutTemplateContent.replace("<!-- REPLACE WITH CSS LINKS -->",
                                                      cssLinksForLayoutTemplate);
        Files.writeString(layoutTemplate, result);
    }

    private void insertJsLinksToLayoutTemplate(Path layoutTemplate,
                                               String jsLinksForLayoutTemplate) throws IOException {
        String layoutTemplateContent = Files.readString(layoutTemplate);
        String result = layoutTemplateContent.replace("<!-- REPLACE WITH JS LINKS -->",
                                                      jsLinksForLayoutTemplate);
        Files.writeString(layoutTemplate, result);
    }

    private void createDefaultIndexTemplate(Path base) throws IOException {
        Path indexTemplate = base.resolve("src/main/resources/templates/index.html");
        Files.createDirectories(indexTemplate.getParent());
        String source = "/files/templates/index.html";
        try (InputStream stream = getClass().getResourceAsStream(source)) {
            Files.copy(Objects.requireNonNull(stream, () -> "Could not find " + source),
                       indexTemplate);
        }
    }

    private void createDefaultApplicationCss(Path base) throws IOException {
        Path path = base.resolve("src/main/resources/static/css/application.css");
        Files.createDirectories(path.getParent());
        if (!Files.exists(path)) {
            Files.writeString(path, "");
        }
    }

}
