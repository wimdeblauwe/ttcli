package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.buildtools.MavenDependency;
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
    public List<MavenDependency> getMavenDependencies(String springBootVersion) {
        return Collections.singletonList(new MavenDependency("org.webjars", "bootstrap", "5.3.3"));
    }

    @Override
    public String getCssLinksForLayoutTemplate() {
        return """
                <link rel="stylesheet" th:href="@{/webjars/bootstrap/css/bootstrap.min.css}">""";
    }

    @Override
    public String getJsLinksForLayoutTemplate() {
        return """
                <script defer th:src="@{/webjars/bootstrap/js/bootstrap.min.js}"></script>""";
    }
}
