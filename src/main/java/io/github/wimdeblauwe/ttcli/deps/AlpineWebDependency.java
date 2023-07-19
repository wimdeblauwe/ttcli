package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.maven.MavenDependency;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AlpineWebDependency implements WebDependency {
    @Override
    public String id() {
        return "alpinejs";
    }

    @Override
    public String displayName() {
        return "Alpine.js";
    }

    @Override
    public List<MavenDependency> getMavenDependencies(String springBootVersion) {
        return Collections.singletonList(new MavenDependency("org.webjars.npm", "alpinejs", "3.12.3"));
    }

    @Override
    public String getCssLinksForLayoutTemplate() {
        return null;
    }

    @Override
    public String getJsLinksForLayoutTemplate() {
        return """
                <script type="text/javascript" th:src="@{/webjars/alpinejs/dist/cdn.min.js}"></script>""";
    }
}
