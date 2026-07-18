package io.github.wimdeblauwe.ttcli.template;

/**
 * Enum representing the supported template engines.
 */
public sealed interface TemplateEngineType {

    String TYPE_THYMELEAF = "thymeleaf";
    String TYPE_JTE = "jte";

    String id();

    record Thymeleaf(boolean useLayoutDialect) implements TemplateEngineType {
        @Override
        public String id() {
            return TYPE_THYMELEAF;
        }
    }

    record Jte() implements TemplateEngineType {
        @Override
        public String id() {
            return TYPE_JTE;
        }
    }
}