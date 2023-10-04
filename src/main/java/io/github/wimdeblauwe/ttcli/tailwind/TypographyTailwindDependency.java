package io.github.wimdeblauwe.ttcli.tailwind;

import org.springframework.stereotype.Component;

/**
 * @see <a href="https://tailwindcss.com/docs/typography-plugin">@tailwindcss/typography</a>
 */
@Component
public class TypographyTailwindDependency implements TailwindDependency {
    @Override
    public String id() {
        return "typography";
    }

    @Override
    public String displayName() {
        return "@tailwindcss/typography";
    }

    @Override
    public String npmPackageName() {
        return "@tailwindcss/typography";
    }

    @Override
    public String pluginName() {
        return "@tailwindcss/typography";
    }
}
