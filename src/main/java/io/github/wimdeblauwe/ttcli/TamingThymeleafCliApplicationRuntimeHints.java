package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.npm.PackageJsonModel;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class TamingThymeleafCliApplicationRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints,
                              ClassLoader classLoader) {
        hints.resources().registerPattern("files/*");
        hints.reflection()
             .registerType(PackageJsonModel.class, MemberCategory.values());
    }
}
