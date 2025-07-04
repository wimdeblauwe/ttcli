package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.maven.MavenDependency;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * See <a href="https://shoelace.style/">https://shoelace.style/</a>
 */
@Component
public class ShoelaceWebDependency implements WebjarsBasedWebDependency {
    @Override
    public String id() {
        return "shoelace";
    }

    @Override
    public String displayName() {
        return "Shoelace";
    }

    @Override
    public List<MavenDependency> getMavenDependencies(String springBootVersion, TemplateEngineType templateEngineType) {
        return Collections.singletonList(new MavenDependency("org.webjars.npm", "shoelace-style__shoelace", "2.20.0"));
    }

    @Override
    public String getCssLinksForLayoutTemplate(TemplateEngineType templateEngineType) {
        return switch (templateEngineType) {
            case THYMELEAF -> """
                <link rel="stylesheet" th:href="@{/webjars/shoelace-style__shoelace/dist/themes/light.css}">""";
            case JTE -> """
                    <link rel="stylesheet" href="/webjars/shoelace-style__shoelace/dist/themes/light.css">""";
        };
    }

    @Override
    public String getJsLinksForLayoutTemplate(TemplateEngineType templateEngineType) {
        return switch (templateEngineType) {
            case THYMELEAF -> """
                <script type="module" th:src="@{/webjars/shoelace-style__shoelace/cdn/shoelace-autoloader.js}"></script>""";
            case JTE -> """
                    <script type="module" th:src="/webjars/shoelace-style__shoelace/cdn/shoelace-autoloader.js"></script>""";
        };
    }
}
