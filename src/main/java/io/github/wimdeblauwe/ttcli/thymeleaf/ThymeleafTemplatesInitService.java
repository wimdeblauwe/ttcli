package io.github.wimdeblauwe.ttcli.thymeleaf;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import io.github.wimdeblauwe.ttcli.deps.WebjarsBasedWebDependency;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceParameters;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
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
        createDefaultLayoutTemplate(basePath, parameters.webDependencies(), parameters.liveReloadInitServiceParameters());
        createDefaultIndexTemplate(basePath);
        createDefaultApplicationCss(basePath);
    }

    private void createDefaultLayoutTemplate(Path base,
                                             List<WebDependency> webDependencies,
                                             LiveReloadInitServiceParameters liveReloadInitServiceParameters) throws IOException {
        Path layoutTemplate = base.resolve("src/main/resources/templates/layout/main.html");
        Files.createDirectories(layoutTemplate.getParent());
        String source = "/files/templates/thymeleaf/layout/main.html";
        try (InputStream stream = getClass().getResourceAsStream(source)) {
            Files.copy(Objects.requireNonNull(stream, () -> "Could not find " + source),
                       layoutTemplate);
        }

        StringBuilder headTags = new StringBuilder();
        if (!liveReloadInitServiceParameters.initServiceId().equals("vite")
            && !liveReloadInitServiceParameters.initServiceId().equals("vite-with-tailwind-css")) {
            headTags.append("""
                                    <link rel="stylesheet" th:href="@{/css/application.css}">""");
        } else {
            headTags.append("""
                                    <vite:client></vite:client>
                                    <vite:vite>
                                      <vite:entry value="/css/application.css"></vite:entry>
                                    </vite:vite>
                                    """);
        }
        insertHeadTagsToLayoutTemplate(layoutTemplate, headTags.toString());

        StringBuilder cssLinksForLayoutTemplate = new StringBuilder();
        for (WebDependency webDependency : webDependencies) {
            if (webDependency instanceof WebjarsBasedWebDependency webjarsBasedWebDependency) {
                String cssForDependency = webjarsBasedWebDependency.getCssLinksForLayoutTemplate(TemplateEngineType.THYMELEAF);
                if (cssForDependency != null) {
                    cssLinksForLayoutTemplate
                            .append('\n')
                            .append(cssForDependency);
                }
            }
        }
        insertCssLinksToLayoutTemplate(layoutTemplate, cssLinksForLayoutTemplate.toString());

        StringBuilder jsLinksForLayoutTemplate = new StringBuilder();
        for (WebDependency webDependency : webDependencies) {
            if (webDependency instanceof WebjarsBasedWebDependency webjarsBasedWebDependency) {
                String jsForDependency = webjarsBasedWebDependency.getJsLinksForLayoutTemplate(TemplateEngineType.THYMELEAF);
                if (jsForDependency != null) {
                    jsLinksForLayoutTemplate
                            .append('\n')
                            .append(jsForDependency);
                }
            }
        }
        insertJsLinksToLayoutTemplate(layoutTemplate, jsLinksForLayoutTemplate.toString());
    }

    private void insertHeadTagsToLayoutTemplate(Path layoutTemplate,
                                                String headTags) throws IOException {
        String layoutTemplateContent = Files.readString(layoutTemplate);
        String result = layoutTemplateContent.replace("<!-- REPLACE WITH HEAD TAGS -->",
                                                      headTags);
        Files.writeString(layoutTemplate, result);
    }

    private void insertCssLinksToLayoutTemplate(Path layoutTemplate,
                                                String cssLinksForLayoutTemplate) throws IOException {
        String layoutTemplateContent = Files.readString(layoutTemplate);
        String result = layoutTemplateContent.replace("<!-- REPLACE WITH EXTERNAL CSS LINKS -->",
                                                      cssLinksForLayoutTemplate);
        Files.writeString(layoutTemplate, result);
    }

    private void insertJsLinksToLayoutTemplate(Path layoutTemplate,
                                               String jsLinksForLayoutTemplate) throws IOException {
        String layoutTemplateContent = Files.readString(layoutTemplate);
        String result = layoutTemplateContent.replace("<!-- REPLACE WITH EXTERNAL JS LINKS -->",
                                                      jsLinksForLayoutTemplate);
        Files.writeString(layoutTemplate, result);
    }

    private void createDefaultIndexTemplate(Path base) throws IOException {
        Path indexTemplate = base.resolve("src/main/resources/templates/index.html");
        Files.createDirectories(indexTemplate.getParent());
        String source = "/files/templates/thymeleaf/index.html";
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
