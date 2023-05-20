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
    public List<MavenDependency> getMavenDependencies(String springBootVersion) {
        String htmxSpringBootThymeleafVersion = getHtmxSpringBootThymeleafVersion(springBootVersion);

        return List.of(
                new MavenDependency("org.webjars.npm", "htmx.org", "1.9.2"),
                new MavenDependency("io.github.wimdeblauwe", "htmx-spring-boot-thymeleaf", htmxSpringBootThymeleafVersion)
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

    private static String getHtmxSpringBootThymeleafVersion(String springBootVersion) {
        String htmxSpringBootThymeleafVersion;
        if (springBootVersion.startsWith("2.")) {
            htmxSpringBootThymeleafVersion = "1.0.0";
        } else if (springBootVersion.startsWith("3.")) {
            htmxSpringBootThymeleafVersion = "2.0.0";
        } else {
            throw new IllegalArgumentException("Unknown Spring Boot version: " + springBootVersion);
        }
        return htmxSpringBootThymeleafVersion;
    }
}
