package io.github.wimdeblauwe.ttcli.npm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PackageJsonModel {
    private String name;
    @JsonProperty("private")
    private boolean privateField;
    private String type;
    private Map<String, String> devDependencies;
    private Map<String, String> scripts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPrivateField() {
        return privateField;
    }

    public void setPrivateField(boolean privateField) {
        this.privateField = privateField;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
