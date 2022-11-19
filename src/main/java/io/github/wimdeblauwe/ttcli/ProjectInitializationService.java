package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.SpringBootInitializrService;
import io.github.wimdeblauwe.ttcli.help.HelpTextInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceFactory;
import io.github.wimdeblauwe.ttcli.maven.MavenInitService;
import io.github.wimdeblauwe.ttcli.thymeleaf.ThymeleafTemplatesInitService;
import io.github.wimdeblauwe.ttcli.util.FileUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ProjectInitializationService {
    private final SpringBootInitializrService initializrService;
    private final LiveReloadInitServiceFactory liveReloadInitServiceFactory;
    private final ThymeleafTemplatesInitService thymeleafTemplatesInitService;
    private final MavenInitService mavenInitService;
    private final HelpTextInitService helpTextInitService;

    public ProjectInitializationService(SpringBootInitializrService initializrService,
                                        LiveReloadInitServiceFactory liveReloadInitServiceFactory,
                                        ThymeleafTemplatesInitService thymeleafTemplatesInitService,
                                        MavenInitService mavenInitService,
                                        HelpTextInitService helpTextInitService) {
        this.initializrService = initializrService;
        this.liveReloadInitServiceFactory = liveReloadInitServiceFactory;
        this.thymeleafTemplatesInitService = thymeleafTemplatesInitService;
        this.mavenInitService = mavenInitService;
        this.helpTextInitService = helpTextInitService;
    }

    public void initialize(ProjectInitializationParameters parameters) throws IOException {
        Path basePath = parameters.basePath();
        if (!FileUtil.isEmpty(basePath)) {
            System.out.println("The directory is not empty! Unable to generate project in " + basePath.toAbsolutePath());
            return;
        }
        if (!Files.exists(basePath)) {
            Files.createDirectories(basePath);
        }

        initializrService.generate(basePath,
                                   parameters.springBootProjectCreationParameters());

        LiveReloadInitService liveReloadInitService = liveReloadInitServiceFactory.getInitService(parameters.liveReloadInitServiceParameters().initServiceId());
        liveReloadInitService.generate(parameters);
        helpTextInitService.addHelpText(basePath, liveReloadInitService.getHelpText());

        mavenInitService.generate(parameters);
        thymeleafTemplatesInitService.generate(parameters);
    }
}
