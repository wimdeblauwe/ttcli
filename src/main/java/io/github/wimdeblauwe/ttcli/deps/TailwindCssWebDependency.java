package io.github.wimdeblauwe.ttcli.deps;

import org.springframework.stereotype.Component;

@Component
public class TailwindCssWebDependency implements WebDependency {
    @Override
    public String id() {
        return "tailwind-css";
    }

    @Override
    public String displayName() {
        return "Tailwind CSS";
    }
}
