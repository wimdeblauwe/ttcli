package io.github.wimdeblauwe.ttcli.livereload;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;

import java.nio.file.Path;
import java.util.Set;

public interface LiveReloadInitService {
    String getId();

    String getName();

    String getHelpText();

    Set<String> additionalSpringInitializrDependencies();

    void generate(ProjectInitializationParameters projectInitializationParameters) throws LiveReloadInitServiceException;

    void runBuild(ProjectInitializationParameters projectInitializationParameters) throws LiveReloadInitServiceException;

    Path getTailwindConfigFileParentPath(ProjectInitializationParameters parameters);

    boolean isApplicableForTemplateEngine(TemplateEngineType templateEngineType);
}
