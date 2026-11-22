package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;

import java.util.List;

/**
 * Builds the script tags of the web dependencies for the layout template.
 */
public final class LayoutTemplateJsLinks {

    private LayoutTemplateJsLinks() {
    }

    /**
     * htmx comes first, so that the {@code hx-alpine-compat} extension of htmx 4 is registered before Alpine.js starts.
     * The extension is only added when Alpine.js is one of the web dependencies.
     */
    public static String create(List<WebDependency> webDependencies, TemplateEngineType templateEngineType) {
        boolean usesAlpine = webDependencies.stream().anyMatch(AlpineWebDependency.class::isInstance);
        StringBuilder result = new StringBuilder();
        webDependencies.stream()
                .filter(HtmxWebDependency.class::isInstance)
                .map(HtmxWebDependency.class::cast)
                .findFirst()
                .ifPresent(htmx -> result.append('\n').append(htmx.getJsLinksForLayoutTemplate(templateEngineType, usesAlpine)));
        for (WebDependency webDependency : webDependencies) {
            if (webDependency instanceof HtmxWebDependency) {
                continue;
            }
            if (webDependency instanceof WebjarsBasedWebDependency webjarsBasedWebDependency) {
                String jsForDependency = webjarsBasedWebDependency.getJsLinksForLayoutTemplate(templateEngineType);
                if (jsForDependency != null) {
                    result.append('\n').append(jsForDependency);
                }
            }
        }
        return result.toString();
    }
}
