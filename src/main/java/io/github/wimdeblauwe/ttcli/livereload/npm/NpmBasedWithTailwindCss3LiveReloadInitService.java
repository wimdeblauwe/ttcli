package io.github.wimdeblauwe.ttcli.livereload.npm;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceException;
import io.github.wimdeblauwe.ttcli.livereload.TailwindCssSpecializedLiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.helper.TailwindCss3Helper;
import io.github.wimdeblauwe.ttcli.npm.NodeService;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindVersion;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class NpmBasedWithTailwindCss3LiveReloadInitService extends NpmBasedLiveReloadInitService implements TailwindCssSpecializedLiveReloadInitService {
    public NpmBasedWithTailwindCss3LiveReloadInitService(NodeService nodeService) {
        super(nodeService);
    }

    @Override
    public String getId() {
        return "npm-based-with-tailwind-css";
    }

    @Override
    public String getName() {
        return "NPM based with Tailwind CSS";
    }

    @Override
    public void generate(ProjectInitializationParameters projectInitializationParameters) {
        try {
            super.generate(projectInitializationParameters);

            TailwindCss3Helper.createApplicationCss(projectInitializationParameters.basePath(),
                    "src/main/resources/static/css/application.css");
            TailwindCss3Helper.setupTailwindConfig(projectInitializationParameters.basePath(), "./src/main/resources/templates/**/*.html");
        } catch (IOException e) {
            throw new LiveReloadInitServiceException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LiveReloadInitServiceException(e);
        }
    }

    @Override
    public Path getTailwindConfigFileParentPath(ProjectInitializationParameters parameters) {
        return parameters.basePath();
    }

    protected String postcssConfigFilePath() {
        return "/files/livereload/npm/npm-based-with-tailwind-css-3/postcss.config.js";
    }

    @Override
    protected List<String> npmDevDependencies() {
        List<String> dependencies = new ArrayList<>(super.npmDevDependencies());
        dependencies.add("tailwindcss@3");
        return dependencies;
    }

    @Override
    protected LinkedHashMap<String, String> npmScripts() {
        LinkedHashMap<String, String> scripts = new LinkedHashMap<>();
        scripts.put("build", "npm-run-all --parallel build:*");
        scripts.put("build:html", "recursive-copy \"src/main/resources/templates\" target/classes/templates -w");
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
    public boolean isTailwindVersionOf(TailwindVersion tailwindVersion, Class<? extends LiveReloadInitService> liveReloadInitServiceClass) {
        return tailwindVersion.equals(TailwindVersion.VERSION_3)
                && liveReloadInitServiceClass.isAssignableFrom(NpmBasedLiveReloadInitService.class);
    }
}
