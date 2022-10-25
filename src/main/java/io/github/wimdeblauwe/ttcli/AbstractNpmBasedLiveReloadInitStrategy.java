package io.github.wimdeblauwe.ttcli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public abstract class AbstractNpmBasedLiveReloadInitStrategy implements LiveReloadInitStrategy {
    @Override
    public void execute(LiveReloadInitParameters parameters) throws IOException, InterruptedException {
        checkIfNodeAndNpmAreInstalled();

        Path base = parameters.baseDir();
        createEmptyPackageJson(base);
        installNpmDependencies(base);
        copyCopyFilesJs(base);
        copyPostcssConfigJs(base);
        insertPackageJsonScripts(base);
        createApplicationCss(base);
        postExecuteNpmPart(base);
    }

    protected void postExecuteNpmPart(Path base) throws IOException, InterruptedException {

    }

    protected abstract List<String> npmDependencies();

    protected abstract LinkedHashMap<String, String> npmScripts();

    protected abstract String applicationCssContent();

    protected abstract String postcssConfigJsSourceFile();

    private void createApplicationCss(Path base) throws IOException {
        Path path = base.resolve("src/main/resources/static/css/application.css");
        Files.createDirectories(path.getParent());
        Files.writeString(path, applicationCssContent());
    }

    private void insertPackageJsonScripts(Path base) throws IOException, InterruptedException {
        System.out.println("\uD83D\uDC77\u200D♂️ Adding build scripts");
        installNpmAddScriptDependency(base);

        addBuildScriptsToPackageJson(base);

        uninstallNpmAddScriptDependency(base);
    }

    private void addBuildScriptsToPackageJson(Path base) throws IOException, InterruptedException {
        for (Map.Entry<String, String> entry : npmScripts().entrySet()) {
            ProcessBuilder builder = new ProcessBuilder("npx", "npmAddScript", "-k", entry.getKey(), "-v", entry.getValue());
            builder.directory(base.toFile());
            Process process = builder.start();
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                throw new RuntimeException("unable to add script entry " + entry + "\n" + new String(process.getErrorStream().readAllBytes()));
            }
        }
    }

    private void installNpmAddScriptDependency(Path base) throws InterruptedException, IOException {
        ProcessBuilder builder = new ProcessBuilder("npm", "install", "-D", "npm-add-script");
        builder.directory(base.toFile());
        int exitValue = builder.start().waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("unable to install npm-add-script");
        }
    }

    private void uninstallNpmAddScriptDependency(Path base) throws InterruptedException, IOException {
        ProcessBuilder builder = new ProcessBuilder("npm", "remove", "-D", "npm-add-script");
        builder.directory(base.toFile());
        int exitValue = builder.start().waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("unable to remove npm-add-script");
        }
    }

    private void copyPostcssConfigJs(Path base) throws IOException {
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream(postcssConfigJsSourceFile())), base.resolve("postcss.config.js"));
    }

    private void copyCopyFilesJs(Path base) throws IOException {
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/files/copy-files.js")), base.resolve("copy-files.js"));
    }

    private void installNpmDependencies(Path base) throws IOException, InterruptedException {
        System.out.println("\uD83D\uDD28 Installing npm dependencies");
        List<String> parameters = new ArrayList<>();
        parameters.addAll(List.of("npm", "install", "-D"));
        parameters.addAll(npmDependencies());
        ProcessBuilder builder = new ProcessBuilder(parameters);
        builder.directory(base.toFile());
        int exitValue = builder.start().waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("installation of npm dependencies failed");
        }
    }

    private void createEmptyPackageJson(Path base) throws IOException {
        Path path = base.resolve("package.json");
        Files.writeString(path, """
                {
                  "name": "my-application"
                }
                """); // TODO read artifactId from pom.xml
    }

    private void checkIfNodeAndNpmAreInstalled() throws IOException, InterruptedException {
        checkIfNpmIsInstalled("node");
        checkIfNpmIsInstalled("npm");
    }

    private void checkIfNpmIsInstalled(String application) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(application, "-v");
        Process process = builder.start();
        int exitValue = process.waitFor();
        String version = new String(process.getInputStream().readAllBytes());
        System.out.println("\uD83D\uDEE0️  Using " + application + " " + version.trim());
        if (exitValue != 0) {
            throw new IllegalArgumentException(application + " is not installed");
        }
    }

}
