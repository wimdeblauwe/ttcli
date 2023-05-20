package io.github.wimdeblauwe.ttcli.boot;

import io.github.wimdeblauwe.ttcli.util.InetUtil;
import io.github.wimdeblauwe.ttcli.util.ZipUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

@Component
public class SpringBootInitializrService {
    private final SpringBootInitializrClient initializrClient;

    public SpringBootInitializrService(SpringBootInitializrClient initializrClient) {
        this.initializrClient = initializrClient;
    }

    public void generate(Path basePath,
                         SpringBootProjectCreationParameters parameters) throws IOException {
        System.out.println("\uD83C\uDF43 Generating Spring Boot project");

        InetUtil.checkIfInternetAccessIsAvailable("start.spring.io");

        byte[] generatedProjectZip = initializrClient.generateProject("maven-project",
                                                                      parameters.springBootVersion(),
                                                                      parameters.groupId(),
                                                                      parameters.artifactId(),
                                                                      parameters.projectName(),
                                                                      Set.of("web", "thymeleaf"));
        ZipUtil.unzip(generatedProjectZip, basePath);
    }
}
