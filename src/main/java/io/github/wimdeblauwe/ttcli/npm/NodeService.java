package io.github.wimdeblauwe.ttcli.npm;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class NodeService {
    public InstalledApplicationVersions checkIfNodeAndNpmAreInstalled() throws IOException, InterruptedException {
        String nodeVersion = checkIfApplicationIsInstalled("node");
        String npmVersion = checkIfApplicationIsInstalled("npm");
        System.out.println("\uD83D\uDEE0️  Using node " + nodeVersion + " with npm " + npmVersion);
        return new InstalledApplicationVersions(nodeVersion,
                                                npmVersion);
    }

    public void createEmptyPackageJson(Path base,
                                       String projectName) throws IOException {
        Path path = base.resolve("package.json");
        Files.writeString(path, """
                {
                  "name": "%s"
                }
                """.formatted(projectName));
    }

    public void installNpmDevDependencies(Path base,
                                          List<String> dependencies) throws IOException, InterruptedException {
        System.out.println("\uD83D\uDD28 Installing npm dependencies");
        List<String> parameters = new ArrayList<>();
        parameters.addAll(List.of("npm", "install", "-D"));
        parameters.addAll(dependencies);
        ProcessBuilder builder = new ProcessBuilder(parameters);
        builder.directory(base.toFile());
        int exitValue = builder.start().waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("installation of npm dependencies failed");
        }
    }

    public void insertPackageJsonScripts(Path base,
                                         Map<String, String> scripts) throws IOException, InterruptedException {
        System.out.println("\uD83D\uDC77\u200D♂️ Adding npm build scripts");
        addBuildScriptsToPackageJson(base, scripts);
    }

    private void addBuildScriptsToPackageJson(Path base,
                                              Map<String, String> scripts) throws IOException {
        PackageJsonReaderWriter packageJsonReaderWriter = PackageJsonReaderWriter.readFrom(base.resolve("package.json"));
        packageJsonReaderWriter.addScripts(scripts);
        packageJsonReaderWriter.write();
    }


    private String checkIfApplicationIsInstalled(String application) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(application, "-v");
        Process process = builder.start();
        int exitValue = process.waitFor();
        String version = new String(process.getInputStream().readAllBytes()).trim();
        if (exitValue != 0) {
            throw new IllegalArgumentException(application + " is not installed");
        }

        return version;
    }

}
