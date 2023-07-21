package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.SpringBootInitializrService;
import io.github.wimdeblauwe.ttcli.help.HelpTextInitService;
import io.github.wimdeblauwe.ttcli.java.JavaCodeInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceFactory;
import io.github.wimdeblauwe.ttcli.maven.MavenInitService;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindDependencyInitService;
import io.github.wimdeblauwe.ttcli.thymeleaf.ThymeleafTemplatesInitService;
import io.github.wimdeblauwe.ttcli.util.FileUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ProjectInitializationService {
    private final SpringBootInitializrService initializrService;
    private final JavaCodeInitService javaCodeInitService;
    private final LiveReloadInitServiceFactory liveReloadInitServiceFactory;
    private final ThymeleafTemplatesInitService thymeleafTemplatesInitService;
    private final MavenInitService mavenInitService;
    private final TailwindDependencyInitService tailwindDependencyInitService;
    private final HelpTextInitService helpTextInitService;

    public ProjectInitializationService(SpringBootInitializrService initializrService,
                                        JavaCodeInitService javaCodeInitService,
                                        LiveReloadInitServiceFactory liveReloadInitServiceFactory,
                                        ThymeleafTemplatesInitService thymeleafTemplatesInitService,
                                        MavenInitService mavenInitService,
                                        TailwindDependencyInitService tailwindDependencyInitService,
                                        HelpTextInitService helpTextInitService) {
        this.initializrService = initializrService;
        this.javaCodeInitService = javaCodeInitService;
        this.liveReloadInitServiceFactory = liveReloadInitServiceFactory;
        this.thymeleafTemplatesInitService = thymeleafTemplatesInitService;
        this.mavenInitService = mavenInitService;
        this.tailwindDependencyInitService = tailwindDependencyInitService;
        this.helpTextInitService = helpTextInitService;
    }

    public void initialize(ProjectInitializationParameters parameters) throws IOException {
        Path basePath = parameters.basePath();
        if (!FileUtil.isEmpty(basePath)) {
            throw new ProjectInitializationServiceException("The directory is not empty! Unable to generate project in " + basePath.toAbsolutePath());
        }
        if (!Files.exists(basePath)) {
            Files.createDirectories(basePath);
        }

        initializrService.generate(basePath,
                                   parameters.springBootProjectCreationParameters());
        javaCodeInitService.generate(parameters);

        LiveReloadInitService liveReloadInitService = liveReloadInitServiceFactory.getInitService(parameters.liveReloadInitServiceParameters().initServiceId());
        liveReloadInitService.generate(parameters);
        helpTextInitService.addHelpText(basePath, liveReloadInitService.getHelpText());

        mavenInitService.generate(parameters);
        tailwindDependencyInitService.generate(parameters);
        thymeleafTemplatesInitService.generate(parameters);

        liveReloadInitService.runBuild(parameters);
    }
}
