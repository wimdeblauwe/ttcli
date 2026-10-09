package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.maven.MavenDependency;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HtmxWebDependency implements WebjarsBasedWebDependency {

    private final HtmxVersion htmxVersion;

    @Autowired
    public HtmxWebDependency() {
        this(HtmxVersion.VERSION_4);
    }

    private HtmxWebDependency(HtmxVersion htmxVersion) {
        this.htmxVersion = htmxVersion;
    }

    public HtmxWebDependency withVersion(HtmxVersion htmxVersion) {
        return new HtmxWebDependency(htmxVersion);
    }

    public HtmxVersion htmxVersion() {
        return htmxVersion;
    }

    /**
     * htmx 4 with Thymeleaf requires htmx-spring-boot 6.x, which needs Spring Boot 4.
     */
    public static boolean supportsHtmx4(String springBootVersion, TemplateEngineType templateEngineType) {
        return templateEngineType instanceof TemplateEngineType.Jte
                || springBootVersion.startsWith("4.");
    }

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
        result.add(new MavenDependency("org.webjars.npm", "htmx.org", getHtmxVersion()));
        if (templateEngineType instanceof TemplateEngineType.Thymeleaf) {
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
            case TemplateEngineType.Thymeleaf _ -> """
                    <script defer th:src="@{/webjars/htmx.org/dist/htmx.min.js}"></script>""";
            case TemplateEngineType.Jte _ -> """
                    <script defer src="/webjars/htmx.org/dist/htmx.min.js"></script>""";
        };
    }

    private String getHtmxVersion() {
        return switch (htmxVersion) {
            case VERSION_2 -> "2.0.11";
            case VERSION_4 -> "4.0.0";
        };
    }

    private String getHtmxSpringBootThymeleafVersion(String springBootVersion) {
        if (htmxVersion == HtmxVersion.VERSION_4) {
            if (!springBootVersion.startsWith("4.")) {
                throw new IllegalArgumentException("htmx 4 requires Spring Boot 4.x, but got: " + springBootVersion);
            }
            return "6.0.0";
        }

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
            htmxSpringBootThymeleafVersion = "4.0.2";
        } else if (springBootVersion.startsWith("4.")) {
            htmxSpringBootThymeleafVersion = "5.2.0";
        } else {
            throw new IllegalArgumentException("Unknown Spring Boot version: " + springBootVersion);
        }
        return htmxSpringBootThymeleafVersion;
    }
}
