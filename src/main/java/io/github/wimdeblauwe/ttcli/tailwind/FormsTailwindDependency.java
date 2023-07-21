package io.github.wimdeblauwe.ttcli.tailwind;

import org.springframework.stereotype.Component;

@Component
public class FormsTailwindDependency implements TailwindDependency {
    @Override
    public String id() {
        return "forms";
    }

    @Override
    public String displayName() {
        return "@tailwindcss/forms";
    }

    @Override
    public String npmPackageName() {
        return "@tailwindcss/forms";
    }

    @Override
    public String pluginName() {
        return "@tailwindcss/forms";
    }
}
