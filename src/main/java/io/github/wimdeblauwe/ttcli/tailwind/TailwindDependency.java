package io.github.wimdeblauwe.ttcli.tailwind;

public interface TailwindDependency {
    String id();

    String displayName();

    String npmPackageName(TailwindVersion tailwindVersion);

    String pluginName();
}
