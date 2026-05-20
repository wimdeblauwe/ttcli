package io.github.wimdeblauwe.ttcli.npm;

public enum PackageManager {
    NPM("npm", "install-node-and-npm", "npm", "npx", "frontend-maven-plugin.npmVersion"),
    PNPM("pnpm", "install-node-and-pnpm", "pnpm", "pnpm dlx", "frontend-maven-plugin.pnpmVersion");

    private final String executable;
    private final String frontendMavenPluginInstallGoal;
    private final String frontendMavenPluginRunGoal;
    private final String packageRunnerCommand;
    private final String mavenVersionProperty;

    PackageManager(String executable,
                   String frontendMavenPluginInstallGoal,
                   String frontendMavenPluginRunGoal,
                   String packageRunnerCommand,
                   String mavenVersionProperty) {
        this.executable = executable;
        this.frontendMavenPluginInstallGoal = frontendMavenPluginInstallGoal;
        this.frontendMavenPluginRunGoal = frontendMavenPluginRunGoal;
        this.packageRunnerCommand = packageRunnerCommand;
        this.mavenVersionProperty = mavenVersionProperty;
    }

    public String executable() {
        return executable;
    }

    public String frontendMavenPluginInstallGoal() {
        return frontendMavenPluginInstallGoal;
    }

    public String frontendMavenPluginRunGoal() {
        return frontendMavenPluginRunGoal;
    }

    public String packageRunnerCommand() {
        return packageRunnerCommand;
    }

    public String mavenVersionProperty() {
        return mavenVersionProperty;
    }

    public String runScriptCommand() {
        return executable + " run";
    }
}
