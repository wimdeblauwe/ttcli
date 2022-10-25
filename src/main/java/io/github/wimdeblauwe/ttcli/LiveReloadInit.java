package io.github.wimdeblauwe.ttcli;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@ShellComponent
public class LiveReloadInit {
    private static final String CSS_FRAMEWORK_BOOTSTRAP = "bootstrap";
    private static final String CSS_FRAMEWORK_TAILWIND_CSS = "tailwindcss";
    @Autowired
    private ComponentFlow.Builder flowBuilder;

    @ShellMethod
    public void liveReloadInit(@ShellOption(defaultValue = ".") String baseDir) throws IOException, InterruptedException {
        Path base = Path.of(baseDir);
        ComponentFlow flow = flowBuilder.clone().reset()
                                        .withSingleItemSelector("css-framework")
                                        .name("CSS Framework to use")
                                        .selectItems(Map.of("Bootstrap", CSS_FRAMEWORK_BOOTSTRAP,
                                                            "Tailwind CSS", CSS_FRAMEWORK_TAILWIND_CSS))
                                        .and()
                                        .build();
        ComponentFlow.ComponentFlowResult flowResult = flow.run();
        String cssFrameworkSelection = flowResult.getContext().get("css-framework");
        if (cssFrameworkSelection.equals(CSS_FRAMEWORK_BOOTSTRAP)) {
            System.out.println("\uD83D\uDC85 Going with bootstrap");
        } else if (cssFrameworkSelection.equals(CSS_FRAMEWORK_TAILWIND_CSS)) {
            System.out.println("\uD83D\uDC85 Going with Tailwind CSS");
        }

        checkIfNodeAndNpmAreInstalled();
        createEmptyPackageJson(base);
        installNpmDependencies(base);
        copyCopyFilesJs(base);
        copyPostcssConfigJs(base);
        insertPackageJsonScripts(base);
    }

    private void insertPackageJsonScripts(Path base) throws IOException, InterruptedException {
        System.out.println("\uD83D\uDC77\u200D♂️ Adding build scripts");
        installNpmAddScriptDependency(base);

        addBuildScriptsToPackageJson(base);

        uninstallNpmAddScriptDependency(base);
    }

    private static void addBuildScriptsToPackageJson(Path base) throws IOException, InterruptedException {
        Map<String, String> scripts = new LinkedHashMap<>();
        scripts.put("build", "npm-run-all --parallel build:*");
        scripts.put("build:html", "node copy-files.js .*\\\\.html$");
        scripts.put("build:css", "mkdirp target/classes/static/css && postcss src/main/resources/static/css/*.css -d target/classes/static/css");
        scripts.put("build:js", "mkdirp target/classes/static/js && babel src/main/resources/static/js/ --out-dir target/classes/static/js/");
        scripts.put("build:svg", "mkdirp target/classes/static/svg && node copy-files.js .*\\\\.svg$");
        scripts.put("build-prod", "NODE_ENV='production' npm-run-all --parallel build-prod:*");
        scripts.put("build-prod:html", "npm run build:html");
        scripts.put("build-prod:css", "npm run build:css");
        scripts.put("build-prod:js", "mkdirp target/classes/static/js && babel src/main/resources/static/js/ --minified --out-dir target/classes/static/js/");
        scripts.put("build-prod:svg", "npm run build:svg");
        scripts.put("watch", "npm-run-all --parallel watch:*");
        scripts.put("watch:html", "onchange \"src/main/resources/templates/**/*.html\" -- npm run build:html");
        scripts.put("watch:css", "onchange \"src/main/resources/static/css/**/*.css\" -- npm run build:css");
        scripts.put("watch:js", "onchange \"src/main/resources/static/js/**/*.js\" -- npm run build:js");
        scripts.put("watch:svg", "onchange \"src/main/resources/static/svg/**/*.svg\" -- npm run build:svg");
        scripts.put("watch:serve", "browser-sync start --proxy localhost:8080 --files \"target/classes/templates\" \"target/classes/static\"");
        for (Map.Entry<String, String> entry : scripts.entrySet()) {
            ProcessBuilder builder = new ProcessBuilder("npx", "npmAddScript", "-k", entry.getKey(), "-v", entry.getValue());
            builder.directory(base.toFile());
            Process process = builder.start();
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                throw new RuntimeException("unable to add script entry " + entry + "\n" + new String(process.getErrorStream().readAllBytes()));
            }
        }
    }

    private static void installNpmAddScriptDependency(Path base) throws InterruptedException, IOException {
        ProcessBuilder builder = new ProcessBuilder("npm", "install", "-D", "npm-add-script");
        builder.directory(base.toFile());
        int exitValue = builder.start().waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("unable to install npm-add-script");
        }
    }

    private static void uninstallNpmAddScriptDependency(Path base) throws InterruptedException, IOException {
        ProcessBuilder builder = new ProcessBuilder("npm", "remove", "-D", "npm-add-script");
        builder.directory(base.toFile());
        int exitValue = builder.start().waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("unable to remove npm-add-script");
        }
    }

    private void copyPostcssConfigJs(Path base) throws IOException {
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/files/postcss.config.js")), base.resolve("postcss.config.js"));
    }

    private void copyCopyFilesJs(Path base) throws IOException {
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/files/copy-files.js")), base.resolve("copy-files.js"));
    }

    private void installNpmDependencies(Path base) throws IOException, InterruptedException {
        System.out.println("\uD83D\uDD28 Installing npm dependencies");
        ProcessBuilder builder = new ProcessBuilder("npm", "install", "-D",
                                                    "@babel/cli", "autoprefixer", "browser-sync", "cssnano",
                                                    "mkdirp", "ncp", "npm-run-all", "onchange", "postcss", "postcss-cli");
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

    private static void checkIfNpmIsInstalled(String application) throws IOException, InterruptedException {
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
