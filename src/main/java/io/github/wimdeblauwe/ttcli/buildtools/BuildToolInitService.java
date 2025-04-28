package io.github.wimdeblauwe.ttcli.buildtools;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;

public interface BuildToolInitService {
    void generate(ProjectInitializationParameters parameters) throws InterruptedException;
    boolean canHandle(ProjectInitializationParameters parameters);
}

