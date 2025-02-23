package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.buildtools.MavenDependency;

import java.util.List;

public interface WebjarsBasedWebDependency extends WebDependency {
    List<MavenDependency> getMavenDependencies(String springBootVersion);

    String getCssLinksForLayoutTemplate();

    String getJsLinksForLayoutTemplate();

}
