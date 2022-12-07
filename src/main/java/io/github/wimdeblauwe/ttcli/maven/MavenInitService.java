package io.github.wimdeblauwe.ttcli.maven;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import org.jsoup.nodes.Comment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class MavenInitService {
    public void generate(ProjectInitializationParameters parameters) {
        try {
            addMavenDependencies(MavenPomReaderWriter.readFrom(parameters.basePath()),
                                 parameters.webDependencies(),
                                 parameters.springBootProjectCreationParameters().springBootVersion());
        } catch (IOException e) {
            throw new MavenInitServiceException(e);
        }
    }

    private void addMavenDependencies(MavenPomReaderWriter mavenPomReaderWriter,
                                      List<WebDependency> webDependencies,
                                      String springBootVersion) throws IOException {
        mavenPomReaderWriter.addDependency("nz.net.ultraq.thymeleaf", "thymeleaf-layout-dialect");
        mavenPomReaderWriter.updateDependencies(dependencies -> {
            dependencies.appendChild(new Comment(" Web dependencies "));
        });
        mavenPomReaderWriter.addDependency("org.webjars", "webjars-locator", "0.46");
        for (WebDependency webDependency : webDependencies) {
            List<MavenDependency> mavenDependencies = webDependency.getMavenDependencies(springBootVersion);
            for (MavenDependency mavenDependency : mavenDependencies) {
                mavenPomReaderWriter.addDependency(mavenDependency);
            }
        }

        mavenPomReaderWriter.write();
    }
}
