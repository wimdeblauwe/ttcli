package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.deps.WebDependency;

import java.util.LinkedHashMap;
import java.util.List;

public class BootstrapLiveReloadInitStrategy extends AbstractNpmBasedLiveReloadInitStrategy {

    public BootstrapLiveReloadInitStrategy(List<WebDependency> webDependencies) {
        super(webDependencies);
    }

    @Override
    protected List<String> npmDependencies() {
        return List.of("@babel/cli", "autoprefixer", "browser-sync", "cssnano",
                       "mkdirp", "ncp", "npm-run-all", "onchange", "postcss", "postcss-cli", "recursive-copy-cli", "path-exists-cli");
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
        scripts.put("watch:html", "onchange \"src/main/resources/templates/**/*.html\" -- npm run build:html");
        scripts.put("watch:css", "onchange \"src/main/resources/static/css/**/*.css\" -- npm run build:css");
        scripts.put("watch:js", "onchange \"src/main/resources/static/js/**/*.js\" -- npm run build:js");
        scripts.put("watch:svg", "onchange \"src/main/resources/static/svg/**/*.svg\" -- npm run build:svg");
        scripts.put("watch:serve", "browser-sync start --proxy localhost:8080 --files \"target/classes/templates\" \"target/classes/static\"");
        return scripts;
    }

    @Override
    protected String applicationCssContent() {
        return "";
    }

    @Override
    protected String postcssConfigJsSourceFile() {
        return "/files/bootstrap/postcss.config.js";
    }

    @Override
    protected void doAddMavenDependencies(MavenPomReaderWriter mavenPomReaderWriter) {
        mavenPomReaderWriter.addDependency("org.webjars", "bootstrap", "5.2.2");
    }

    @Override
    protected String getCssLinksForLayoutTemplate() {
        return """
                <link rel="stylesheet" th:href="@{/webjars/bootstrap/css/bootstrap.min.css}">""";
    }

    @Override
    protected String getJsLinksForLayoutTemplate() {
        return """
                <script defer th:src="@{/webjars/bootstrap/js/bootstrap.min.js}"></script>""";
    }
}
