package io.github.wimdeblauwe.ttcli;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class TamingThymeleafCliApplicationRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints,
                              ClassLoader classLoader) {
        hints.resources().registerPattern("files/*");
    }
}
