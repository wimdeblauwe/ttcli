package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.maven.MavenDependency;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AlpineWebDependency implements WebjarsBasedWebDependency {
    @Override
    public String id() {
        return "alpinejs";
    }

    @Override
    public String displayName() {
        return "Alpine.js";
    }

    @Override
    public List<MavenDependency> getMavenDependencies(String springBootVersion, TemplateEngineType templateEngineType) {
        return Collections.singletonList(new MavenDependency("org.webjars.npm", "alpinejs", "3.15.1"));
    }

    @Override
    public String getCssLinksForLayoutTemplate(TemplateEngineType templateEngineType) {
        return null;
    }

    @Override
    public String getJsLinksForLayoutTemplate(TemplateEngineType templateEngineType) {
        return switch (templateEngineType) {
            case THYMELEAF -> """
                <script type="text/javascript" th:src="@{/webjars/alpinejs/dist/cdn.min.js}"></script>""";
            case JTE -> """
                    <script type="text/javascript" src="/webjars/alpinejs/dist/cdn.min.js"></script>""";
        };
    }
}
