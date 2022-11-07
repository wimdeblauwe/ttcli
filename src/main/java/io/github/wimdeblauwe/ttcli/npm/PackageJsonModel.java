package io.github.wimdeblauwe.ttcli.npm;

import java.util.LinkedHashMap;
import java.util.Map;

public class PackageJsonModel {
    private String name;
    private Map<String, String> devDependencies;
    private Map<String, String> scripts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getDevDependencies() {
        return devDependencies;
    }

    public void setDevDependencies(Map<String, String> devDependencies) {
        this.devDependencies = devDependencies;
    }

    public Map<String, String> getScripts() {
        if (scripts == null) {
            scripts = new LinkedHashMap<>();
        }
        return scripts;
    }

    public void setScripts(Map<String, String> scripts) {
        this.scripts = scripts;
    }

    public void addScript(String name,
                          String content) {
        getScripts().put(name, content);
    }

    public void addScripts(Map<String, String> scripts) {
        getScripts().putAll(scripts);
    }
}
