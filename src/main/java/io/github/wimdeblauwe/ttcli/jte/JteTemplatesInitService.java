package io.github.wimdeblauwe.ttcli.jte;

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
public class JteTemplatesInitService {

    public void generate(ProjectInitializationParameters parameters) throws IOException {
        Path basePath = parameters.basePath();
        createDefaultLayoutTemplate(basePath, parameters.webDependencies(), parameters.liveReloadInitServiceParameters());
        createDefaultIndexTemplate(basePath);
        createDefaultApplicationCss(basePath);
        createDotJteRootFile(basePath);
    }

    private void createDefaultLayoutTemplate(Path base,
                                             List<WebDependency> webDependencies,
                                             LiveReloadInitServiceParameters liveReloadInitServiceParameters) throws IOException {
        Path layoutTemplate = base.resolve("src/main/jte/layout/main.jte");
        Files.createDirectories(layoutTemplate.getParent());
        String source = "/files/templates/jte/layout/main.jte";
        try (InputStream stream = getClass().getResourceAsStream(source)) {
            Files.copy(Objects.requireNonNull(stream, () -> "Could not find " + source),
                    layoutTemplate);
        }

        StringBuilder headTags = new StringBuilder();
        if (!liveReloadInitServiceParameters.initServiceId().equals("vite")
                && !liveReloadInitServiceParameters.initServiceId().equals("vite-with-tailwind-css")) {
            headTags.append("""
                    <link rel="stylesheet" href="/css/application.css">""");
        } else {
            headTags.append("""
                    ${viteClient()}
                    ${viteEntries(
                        "/css/application.css"
                    )}
                    """);
        }
        insertHeadTagsToLayoutTemplate(layoutTemplate, headTags.toString());

        StringBuilder cssLinksForLayoutTemplate = new StringBuilder();
        for (WebDependency webDependency : webDependencies) {
            if (webDependency instanceof WebjarsBasedWebDependency webjarsBasedWebDependency) {
                String cssForDependency = webjarsBasedWebDependency.getCssLinksForLayoutTemplate(TemplateEngineType.JTE);
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
                String jsForDependency = webjarsBasedWebDependency.getJsLinksForLayoutTemplate(TemplateEngineType.JTE);
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
        result = result.replace("<%-- REPLACE WITH IMPORTS --%>",
                "@import static io.github.wimdeblauwe.vite.spring.boot.jte.ViteJte.*");
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
        Path indexTemplate = base.resolve("src/main/jte/index.jte");
        Files.createDirectories(indexTemplate.getParent());
        String source = "/files/templates/jte/index.jte";
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

    private void createDotJteRootFile(Path base) throws IOException {
        Path path = base.resolve("src/main/jte");
        Files.createDirectories(path);
        Path dotFile = path.resolve(".jteroot");
        if (!Files.exists(dotFile)) {
            Files.writeString(dotFile, "");
        }
    }

}
