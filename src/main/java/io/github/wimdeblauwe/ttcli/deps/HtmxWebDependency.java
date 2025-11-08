package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.maven.MavenDependency;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HtmxWebDependency implements WebjarsBasedWebDependency {

    @Override
    public String id() {
        return "htmx";
    }

    @Override
    public String displayName() {
        return "Htmx";
    }

    @Override
    public List<MavenDependency> getMavenDependencies(String springBootVersion, TemplateEngineType templateEngineType) {

        List<MavenDependency> result = new ArrayList<>();
        result.add(new MavenDependency("org.webjars.npm", "htmx.org", "2.0.8"));
        if (templateEngineType == TemplateEngineType.THYMELEAF) {
            String htmxSpringBootThymeleafVersion = getHtmxSpringBootThymeleafVersion(springBootVersion);
            result.add(new MavenDependency("io.github.wimdeblauwe", "htmx-spring-boot-thymeleaf", htmxSpringBootThymeleafVersion));
        }

        return result;
    }

    @Override
    public String getCssLinksForLayoutTemplate(TemplateEngineType templateEngineType) {
        return null;
    }

    @Override
    public String getJsLinksForLayoutTemplate(TemplateEngineType templateEngineType) {
        return switch (templateEngineType) {
            case THYMELEAF -> """
                <script type="text/javascript" th:src="@{/webjars/htmx.org/dist/htmx.min.js}"></script>""";
            case JTE -> """
                    <script type="text/javascript" src="/webjars/htmx.org/dist/htmx.min.js"></script>""";
        };
    }

    private static String getHtmxSpringBootThymeleafVersion(String springBootVersion) {
        String htmxSpringBootThymeleafVersion;
        if (springBootVersion.startsWith("2.")) {
            htmxSpringBootThymeleafVersion = "1.0.0";
        } else if (springBootVersion.startsWith("3.0")
                || springBootVersion.startsWith("3.1")
                || springBootVersion.startsWith("3.2")
                || springBootVersion.startsWith("3.3")) {
            htmxSpringBootThymeleafVersion = "3.6.3";
        } else if (springBootVersion.startsWith("3.")) {
            // From Spring Boot 3.4 onwards, we can use version 4.x of htmx-spring-boot
            htmxSpringBootThymeleafVersion = "4.0.1";
        } else if (springBootVersion.startsWith("4.")) {
            htmxSpringBootThymeleafVersion = "5.0.0-rc.1";
        } else {
            throw new IllegalArgumentException("Unknown Spring Boot version: " + springBootVersion);
        }
        return htmxSpringBootThymeleafVersion;
    }
}
