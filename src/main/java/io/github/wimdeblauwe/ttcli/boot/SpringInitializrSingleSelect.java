package io.github.wimdeblauwe.ttcli.boot;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SpringInitializrSingleSelect {
    @JsonProperty("default")
    private String defaultId;
    private List<IdAndName> values;

    public String getDefaultId() {
        return defaultId;
    }

    public String getDefaultName() {
        return values.stream().filter(idAndName -> idAndName.id().equals(defaultId))
                     .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown id: " + defaultId))
                     .name();
    }

    public void setDefaultId(String defaultId) {
        this.defaultId = defaultId;
    }

    public List<IdAndName> getValues() {
        return values;
    }

    public void setValues(List<IdAndName> values) {
        this.values = values;
    }
}
