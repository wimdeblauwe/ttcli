package io.github.wimdeblauwe.ttcli.buildtools.gradle.groovy;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.boot.SpringBootProjectType;
import io.github.wimdeblauwe.ttcli.buildtools.BuildToolInitService;
import org.springframework.stereotype.Component;

@Component
public class GradleGroovyInitService implements BuildToolInitService {
    @Override
    public void generate(ProjectInitializationParameters parameters) throws InterruptedException {

    }

    @Override
    public boolean canHandle(ProjectInitializationParameters parameters) {
        return parameters.springBootProjectCreationParameters().type().equals(SpringBootProjectType.GRADLE_GROOVY);
    }
}

