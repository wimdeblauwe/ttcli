package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.maven.MavenDependency;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HtmxWebDependency implements WebDependency {

    @Override
    public String id() {
        return "htmx";
    }

    @Override
    public String displayName() {
        return "Htmx";
    }

    @Override
    public List<MavenDependency> getMavenDependencies() {
        return List.of(
                new MavenDependency("org.webjars.npm", "htmx.org", "1.8.0"),
                new MavenDependency("io.github.wimdeblauwe", "htmx-spring-boot-thymeleaf", "0.2.0")
        );
    }

    @Override
    public String getCssLinksForLayoutTemplate() {
        return null;
    }

    @Override
    public String getJsLinksForLayoutTemplate() {
        return """
                <script type="text/javascript" th:src="@{/webjars/htmx.org/dist/htmx.min.js}"></script>""";
    }
}
