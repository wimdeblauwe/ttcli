package io.github.wimdeblauwe.ttcli.npm;

import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceException;
import io.github.wimdeblauwe.ttcli.util.ExternalProcessException;
import io.github.wimdeblauwe.ttcli.util.ExternalProcessRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class NodeService {
    public InstalledApplicationVersions checkIfNodeAndPackageManagerAreInstalled(PackageManager packageManager) throws IOException, InterruptedException {
        String nodeVersion = checkIfApplicationIsInstalled("node");
        String packageManagerVersion = checkIfApplicationIsInstalled(packageManager.executable());
        System.out.println("🛠️  Using node " + nodeVersion + " with " + packageManager.executable() + " " + packageManagerVersion);
        InstalledApplicationVersions versions = new InstalledApplicationVersions(nodeVersion,
                packageManager,
                packageManagerVersion);
        if (versions.nodeVersionBelowCurrentLtsVersion()) {
            throw new LiveReloadInitServiceException("Your node version is below the recommended version. Please upgrade to the latest LTS version.");
        }
        return versions;
    }

    public void createPackageJson(Path base,
                                  String projectName) throws IOException {
        Path path = base.resolve("package.json");
        // The name field in package.json can only be lowercase and cannot contain spaces
        String name = projectName.toLowerCase(Locale.ROOT).replace(' ', '-');
        Files.writeString(path, """
                {
                  "name": "%s"
                }
                """.formatted(name));
    }

    public void createPackageJsonForModule(Path base,
                                           String projectName) throws IOException {
        Path path = base.resolve("package.json");
        // The name field in package.json can only be lowercase and cannot contain spaces
        String name = projectName.toLowerCase(Locale.ROOT).replace(' ', '-');
        Files.writeString(path, """
                {
                  "name": "%s",
                  "private": true,
                  "type": "module"
                }
                """.formatted(name));
    }

    public void installDevDependencies(PackageManager packageManager,
                                       Path base,
                                       List<String> dependencies) throws IOException, InterruptedException {
        System.out.println("🔨 Installing " + packageManager.executable() + " dependencies");
        List<String> parameters = new ArrayList<>();
        parameters.addAll(List.of(packageManager.executable(), "install", "-D"));
        parameters.addAll(dependencies);
        ExternalProcessRunner.run(base.toFile(), parameters, () -> "Installation of " + packageManager.executable() + " dependencies failed");
    }

    public void runPackageRunnerCommand(PackageManager packageManager,
                                        Path base,
                                        List<String> params) throws IOException, InterruptedException {
        List<String> parameters = new ArrayList<>();
        // PackageRunnerCommand may be 'npx' or 'pnpm dlx' — split on space so both work.
        for (String part : packageManager.packageRunnerCommand().split(" ")) {
            parameters.add(part);
        }
        parameters.addAll(params);
        ExternalProcessRunner.run(base.toFile(), parameters, () -> packageManager.packageRunnerCommand() + " command failed");
    }

    public void insertPackageJsonScripts(Path base,
                                         Map<String, String> scripts) throws IOException, InterruptedException {
        System.out.println("👷‍♂️ Adding build scripts to package.json");
        addBuildScriptsToPackageJson(base, scripts);
    }

    private void addBuildScriptsToPackageJson(Path base,
                                              Map<String, String> scripts) throws IOException {
        PackageJsonReaderWriter packageJsonReaderWriter = PackageJsonReaderWriter.readFrom(base.resolve("package.json"));
        packageJsonReaderWriter.addScripts(scripts);
        packageJsonReaderWriter.write();
    }

    public void setPnpmOnlyBuiltDependencies(Path base,
                                             List<String> onlyBuiltDependencies) throws IOException {
        PackageJsonReaderWriter packageJsonReaderWriter = PackageJsonReaderWriter.readFrom(base.resolve("package.json"));
        packageJsonReaderWriter.setPnpmOnlyBuiltDependencies(onlyBuiltDependencies);
        packageJsonReaderWriter.write();
    }

    private String checkIfApplicationIsInstalled(String application) throws InterruptedException {
        try {
            String output = ExternalProcessRunner.run(null, List.of(application, "-v"), () -> String.format("Seems the application '%s' is not installed, which is a prerequisite for the ttcli tool.", application));
            return output.trim();
        } catch (ExternalProcessException e) {
            throw new LiveReloadInitServiceException(String.format("Seems the application '%s' is not installed, which is a prerequisite for the ttcli tool.", application));
        }
    }

}
