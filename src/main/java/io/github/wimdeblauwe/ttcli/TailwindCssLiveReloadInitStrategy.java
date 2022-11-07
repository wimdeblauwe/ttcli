package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.deps.WebDependency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

public class TailwindCssLiveReloadInitStrategy extends AbstractNpmBasedLiveReloadInitStrategy {

    public TailwindCssLiveReloadInitStrategy(List<WebDependency> webDependencies) {
        super(webDependencies);
    }

    @Override
    protected List<String> npmDependencies() {
        return List.of("@babel/cli", "autoprefixer", "browser-sync", "cssnano",
                       "mkdirp", "ncp", "npm-run-all", "onchange", "postcss", "postcss-cli", "recursive-copy-cli", "path-exists-cli",
                       "tailwindcss");
    }

    @Override
    protected LinkedHashMap<String, String> npmScripts() {
        LinkedHashMap<String, String> scripts = new LinkedHashMap<>();
        scripts.put("build", "npm-run-all --parallel build:*");
        scripts.put("build:html", "recursive-copy \"src/main/resources/templates\" target/classes/templates -w -f \"**/*.html\"");
        scripts.put("build:css", "mkdirp target/classes/static/css && postcss src/main/resources/static/css/*.css -d target/classes/static/css");
        scripts.put("build:js", "path-exists src/main/resources/static/js && (mkdirp target/classes/static/js && babel src/main/resources/static/js/ --out-dir target/classes/static/js/) || echo \"No 'src/main/resources/static/js' directory found.\"");
        scripts.put("build:svg", "path-exists src/main/resources/static/svg && recursive-copy \"src/main/resources/static/svg\" target/classes/static/svg -w -f \"**/*.svg\" || echo \"No 'src/main/resources/static/svg' directory found.\"");
        scripts.put("build-prod", "NODE_ENV='production' npm-run-all --parallel build-prod:*");
        scripts.put("build-prod:html", "npm run build:html");
        scripts.put("build-prod:css", "npm run build:css");
        scripts.put("build-prod:js", "path-exists src/main/resources/static/js && (mkdirp target/classes/static/js && babel src/main/resources/static/js/ --minified --out-dir target/classes/static/js/) || echo \"No 'src/main/resources/static/js' directory found.\"");
        scripts.put("build-prod:svg", "npm run build:svg");
        scripts.put("watch", "npm-run-all --parallel watch:*");
        scripts.put("watch:html", "onchange \"src/main/resources/templates/**/*.html\" -- npm-run-all --serial build:css build:html");
        scripts.put("watch:css", "onchange \"src/main/resources/static/css/**/*.css\" -- npm run build:css");
        scripts.put("watch:js", "onchange \"src/main/resources/static/js/**/*.js\" -- npm run build:js");
        scripts.put("watch:svg", "onchange \"src/main/resources/static/svg/**/*.svg\" -- npm run build:svg");
        scripts.put("watch:serve", "browser-sync start --no-inject-changes --proxy localhost:8080 --files \"target/classes/templates\" \"target/classes/static\"");
        return scripts;
    }

    @Override
    protected String applicationCssContent() {
        return """
                @tailwind base;
                @tailwind components;
                @tailwind utilities;""";
    }

    @Override
    protected String postcssConfigJsSourceFile() {
        return "/files/tailwindcss/postcss.config.js";
    }

    @Override
    protected void postExecuteNpmPart(Path base) throws IOException, InterruptedException {
        initializeTailwindConfig(base);

        // Point tailwind to Thymeleaf templates
        Path tailwindConfigFilePath = base.resolve("tailwind.config.js");
        byte[] bytes = Files.readAllBytes(tailwindConfigFilePath);
        String s = new String(bytes);
        s = s.replaceFirst("content: \\[]", "content: ['./src/main/resources/templates/**/*.html']");
        Files.writeString(tailwindConfigFilePath, s);
    }

    private static void initializeTailwindConfig(Path base) throws InterruptedException, IOException {
        ProcessBuilder builder = new ProcessBuilder("npx", "tailwindcss", "init");
        builder.directory(base.toFile());
        int exitValue = builder.start().waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("unable to init tailwind css");
        }
    }
}
