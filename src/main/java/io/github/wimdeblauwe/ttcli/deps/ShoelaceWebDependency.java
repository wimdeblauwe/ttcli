package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.maven.MavenDependency;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * See <a href="https://shoelace.style/">https://shoelace.style/</a>
 */
@Component
public class ShoelaceWebDependency implements WebDependency {
    @Override
    public String id() {
        return "shoelace";
    }

    @Override
    public String displayName() {
        return "Shoelace";
    }

    @Override
    public List<MavenDependency> getMavenDependencies(String springBootVersion) {
        return Collections.singletonList(new MavenDependency("org.webjars.npm", "shoelace-style__shoelace", "2.17.1"));
    }

    @Override
    public String getCssLinksForLayoutTemplate() {
        return """
                <link rel="stylesheet" th:href="@{/webjars/shoelace-style__shoelace/dist/themes/light.css}">""";
    }

    @Override
    public String getJsLinksForLayoutTemplate() {
        return """
                <script type="module" th:src="@{/webjars/shoelace-style__shoelace/cdn/shoelace-autoloader.js}"></script>""";
    }
}
