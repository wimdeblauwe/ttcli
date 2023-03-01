package io.github.wimdeblauwe.ttcli.java;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Component
public class JavaCodeInitService {
    public void generate(ProjectInitializationParameters parameters) throws IOException {
        Path basePath = parameters.basePath();
        String packageName = parameters.springBootProjectCreationParameters().packageName();
        Path layoutTemplate = basePath.resolve("src/main/java/" + packageName.replace(".", "/") + "/HomeController.java");
        Files.createDirectories(layoutTemplate.getParent());
        String source = "/files/java/HomeController.java";
        try (InputStream stream = getClass().getResourceAsStream(source)) {
            Files.copy(Objects.requireNonNull(stream, () -> "Could not find " + source),
                       layoutTemplate);
        }

        String layoutTemplateContent = Files.readString(layoutTemplate);
        String result = layoutTemplateContent.replace("$$PACKAGE_NAME$$",
                                                      packageName);
        Files.writeString(layoutTemplate, result);

    }
}
