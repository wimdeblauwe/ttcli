package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.maven.MavenDependency;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * See <a href="https://shadleaf.dev/">https://shadleaf.dev/</a>
 * <p>
 * Shadleaf is not a webjar, but a Spring Boot starter. Its CSS and JS are rendered by the {@code sl/layout}
 * fragments, which need to be placed before the application stylesheet, so they are not returned as
 * CSS or JS links here.
 */
@Component
public class ShadleafWebDependency implements WebjarsBasedWebDependency {

    public static final String SHADLEAF_VERSION = "0.10.0";

    @Override
    public String id() {
        return "shadleaf";
    }

    @Override
    public String displayName() {
        return "Shadleaf";
    }

    /**
     * Shadleaf needs Thymeleaf and Spring Boot 4.1 or higher.
     */
    @Override
    public boolean isApplicable(String springBootVersion, TemplateEngineType templateEngineType) {
        return templateEngineType instanceof TemplateEngineType.Thymeleaf
                && springBootVersion.startsWith("4.")
                && !springBootVersion.startsWith("4.0");
    }

    @Override
    public List<MavenDependency> getMavenDependencies(String springBootVersion, TemplateEngineType templateEngineType) {
        return Collections.singletonList(new MavenDependency("io.github.wimdeblauwe", "shadleaf-spring-boot-starter", SHADLEAF_VERSION));
    }

    @Override
    public String getCssLinksForLayoutTemplate(TemplateEngineType templateEngineType) {
        return null;
    }

    @Override
    public String getJsLinksForLayoutTemplate(TemplateEngineType templateEngineType) {
        return null;
    }

    /**
     * Shadleaf bundles Alpine.js, so a separately selected Alpine.js dependency is dropped when Shadleaf is selected.
     */
    public static List<WebDependency> removeAlpineIfShadleafIsSelected(List<WebDependency> selectedWebDependencies) {
        if (selectedWebDependencies.stream().noneMatch(webDependency -> webDependency instanceof ShadleafWebDependency)) {
            return selectedWebDependencies;
        }
        return selectedWebDependencies.stream()
                .filter(webDependency -> !(webDependency instanceof AlpineWebDependency))
                .toList();
    }
}
