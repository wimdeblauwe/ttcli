package io.github.wimdeblauwe.ttcli.boot;

public record SpringBootProjectCreationParameters(String groupId,
                                                  String artifactId,
                                                  String projectName,
                                                  String springBootVersion) {
    public String packageName() {
        return groupId + "." + artifactId.replace("-", "");
    }
}
