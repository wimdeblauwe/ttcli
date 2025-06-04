package io.github.wimdeblauwe.ttcli.maven;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import io.github.wimdeblauwe.ttcli.deps.WebjarsBasedWebDependency;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import org.jsoup.nodes.Comment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class MavenInitService {
    public void generate(ProjectInitializationParameters parameters) throws InterruptedException {
        try {
            addMavenDependencies(MavenPomReaderWriter.readFrom(parameters.basePath()),
                    parameters.webDependencies(),
                    parameters.springBootProjectCreationParameters().springBootVersion(),
                    parameters.templateEngineType());
        } catch (IOException e) {
            throw new MavenInitServiceException(e);
        }
    }

    private void addMavenDependencies(MavenPomReaderWriter mavenPomReaderWriter,
                                      List<WebDependency> webDependencies,
                                      String springBootVersion,
                                      TemplateEngineType templateEngineType) throws IOException, InterruptedException {
        // Add template engine specific dependencies
        if (templateEngineType == TemplateEngineType.THYMELEAF) {
            mavenPomReaderWriter.addDependency("nz.net.ultraq.thymeleaf", "thymeleaf-layout-dialect");
        }
        mavenPomReaderWriter.updateDependencies(dependencies -> {
            dependencies.appendChild(new Comment(" Web dependencies "));
        });
        if (springBootVersion.startsWith("1.")
                || springBootVersion.startsWith("2.")
                || springBootVersion.startsWith("3.0")
                || springBootVersion.startsWith("3.1")
                || springBootVersion.startsWith("3.2")
                || springBootVersion.startsWith("3.3")
        ) {
            mavenPomReaderWriter.addDependency("org.webjars", "webjars-locator", "0.52");
        } else {
            // Starting with Spring Boot 3.4.0, we can use webjars-locator-lite
            mavenPomReaderWriter.addDependency("org.webjars", "webjars-locator-lite", "1.0.1");
        }
        for (WebDependency webDependency : webDependencies) {
            if (webDependency instanceof WebjarsBasedWebDependency webjarsBasedWebDependency) {
                List<MavenDependency> mavenDependencies = webjarsBasedWebDependency.getMavenDependencies(springBootVersion, templateEngineType);
                for (MavenDependency mavenDependency : mavenDependencies) {
                    mavenPomReaderWriter.addDependency(mavenDependency);
                }
            }
        }

        mavenPomReaderWriter.write();
    }
}
