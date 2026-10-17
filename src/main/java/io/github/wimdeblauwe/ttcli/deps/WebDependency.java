package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;

public interface WebDependency {
    String id();

    String displayName();

    /**
     * Whether this dependency can be offered for the given Spring Boot version and template engine.
     */
    default boolean isApplicable(String springBootVersion, TemplateEngineType templateEngineType) {
        return true;
    }
}
