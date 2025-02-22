package io.github.wimdeblauwe.ttcli.tailwind;

import org.springframework.stereotype.Component;

/**
 * @see <a href="https://tailwindcss.com/docs/typography-plugin">@tailwindcss/typography</a>
 */
@Component
public class ContainerQueriesTailwindDependency implements TailwindDependency {
    @Override
    public String id() {
        return "container-queries";
    }

    @Override
    public String displayName() {
        return "@tailwindcss/container-queries";
    }

    @Override
    public String npmPackageName(TailwindVersion tailwindVersion) {
        return "@tailwindcss/container-queries";
    }

    @Override
    public String pluginName() {
        return "@tailwindcss/container-queries";
    }
}
