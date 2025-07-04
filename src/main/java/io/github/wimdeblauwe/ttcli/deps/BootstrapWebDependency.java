package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.maven.MavenDependency;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class BootstrapWebDependency implements WebjarsBasedWebDependency {
    @Override
    public String id() {
        return "bootstrap";
    }

    @Override
    public String displayName() {
        return "Bootstrap";
    }

    @Override
    public List<MavenDependency> getMavenDependencies(String springBootVersion, TemplateEngineType templateEngineType) {
        return Collections.singletonList(new MavenDependency("org.webjars", "bootstrap", "5.3.3"));
    }

    @Override
    public String getCssLinksForLayoutTemplate(TemplateEngineType templateEngineType) {
        return switch (templateEngineType) {
            case THYMELEAF -> """
                    <link rel="stylesheet" th:href="@{/webjars/bootstrap/css/bootstrap.min.css}">""";
            case JTE -> """
                    <link rel="stylesheet" href="/webjars/bootstrap/css/bootstrap.min.css">""";
        };
    }

    @Override
    public String getJsLinksForLayoutTemplate(TemplateEngineType templateEngineType) {
        return switch (templateEngineType) {
            case THYMELEAF -> """
                <script defer th:src="@{/webjars/bootstrap/js/bootstrap.min.js}"></script>""";
            case JTE -> """
                    <script defer src="/webjars/bootstrap/js/bootstrap.min.js"></script>""";
        };
    }
}
