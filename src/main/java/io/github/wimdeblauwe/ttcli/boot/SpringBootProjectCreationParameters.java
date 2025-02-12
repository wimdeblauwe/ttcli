package io.github.wimdeblauwe.ttcli.boot;

public record SpringBootProjectCreationParameters(SpringBootProjectType type,
                                                  String groupId,
                                                  String artifactId,
                                                  String projectName,
                                                  String springBootVersion,
                                                  String javaVersion) {
    public String packageName() {
        return groupId + "." + artifactId.replace("-", "_");
    }
}
