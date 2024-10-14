package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.maven.MavenDependency;

import java.util.List;

public interface WebjarsBasedWebDependency extends WebDependency {
    List<MavenDependency> getMavenDependencies(String springBootVersion);

    String getCssLinksForLayoutTemplate();

    String getJsLinksForLayoutTemplate();

}
