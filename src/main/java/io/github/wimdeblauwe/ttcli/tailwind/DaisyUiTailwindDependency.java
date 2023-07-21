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
    public String npmPackageName() {
        return "daisyui@latest";
    }

    @Override
    public String pluginName() {
        return "daisyui";
    }
}
