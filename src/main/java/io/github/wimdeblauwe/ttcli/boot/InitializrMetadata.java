package io.github.wimdeblauwe.ttcli.boot;

public class InitializrMetadata {
    private SpringInitializrSingleSelect bootVersion;
    private SpringInitializrSingleSelect javaVersion;

    public SpringInitializrSingleSelect getBootVersion() {
        return bootVersion;
    }

    public void setBootVersion(SpringInitializrSingleSelect bootVersion) {
        this.bootVersion = bootVersion;
    }

    public SpringInitializrSingleSelect getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(SpringInitializrSingleSelect javaVersion) {
        this.javaVersion = javaVersion;
    }
}
