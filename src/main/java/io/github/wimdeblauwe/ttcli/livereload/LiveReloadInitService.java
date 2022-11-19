package io.github.wimdeblauwe.ttcli.livereload;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;

public interface LiveReloadInitService {
    String getId();

    String getName();

    String getHelpText();

    void generate(ProjectInitializationParameters projectInitializationParameters) throws LiveReloadInitServiceException;
}
