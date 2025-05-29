package io.github.wimdeblauwe.ttcli.boot;

public enum SpringBootProjectType {
    //TODO enable this values to allow gradle build
    GRADLE_GROOVY("gradle-project", "Gradle - Groovy"),
    //GRADLE_KOTLIN("gradle-project-kotlin", "Gradle - Kotlin"),
    MAVEN("maven-project", "Maven");

    private final String value;
    private final String description;

    SpringBootProjectType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String value() {
        return value;
    }

    public String description() {
        return description;
    }

    public static SpringBootProjectType from(String description) {
        switch (description) {
            case "Maven":
                return MAVEN;
            //TODO enable this case to allow gradle build
            case "Gradle - Groovy":
                return GRADLE_GROOVY;
            //case "Gradle - Kotlin":
            //    return GRADLE_KOTLIN;
            default:
                throw new IllegalArgumentException("No Spring Boot project type found for description: " + description);
        }
    }

}
