package io.github.wimdeblauwe.ttcli;

import java.util.LinkedHashMap;
import java.util.List;

public class BootstrapLiveReloadInitStrategy extends AbstractNpmBasedLiveReloadInitStrategy {

    @Override
    protected List<String> npmDependencies() {
        return List.of("@babel/cli", "autoprefixer", "browser-sync", "cssnano",
                       "mkdirp", "ncp", "npm-run-all", "onchange", "postcss", "postcss-cli");
    }

    @Override
    protected LinkedHashMap<String, String> npmScripts() {
        LinkedHashMap<String, String> scripts = new LinkedHashMap<>();
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
}
