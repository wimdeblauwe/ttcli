package io.github.wimdeblauwe.ttcli.deps;

import io.github.wimdeblauwe.ttcli.maven.MavenDependency;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;

import java.util.List;

public interface WebjarsBasedWebDependency extends WebDependency {
    List<MavenDependency> getMavenDependencies(String springBootVersion, TemplateEngineType templateEngineType);

    String getCssLinksForLayoutTemplate();

    String getJsLinksForLayoutTemplate();

}
