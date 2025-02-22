package io.github.wimdeblauwe.ttcli.tailwind;

import org.springframework.stereotype.Component;

@Component
public class DaisyUiTailwindDependency implements TailwindDependency{
    @Override
    public String id() {
        return "daisy-ui";
    }

    @Override
    public String displayName() {
        return "daisyUI";
    }

    @Override
    public String npmPackageName(TailwindVersion tailwindVersion) {
        return switch (tailwindVersion) {
            case VERSION_3 -> "daisyui@4";
            case VERSION_4 -> "daisyui@beta";
        };
    }

    @Override
    public String pluginName() {
        return "daisyui";
    }
}
