package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.SpringBootInitializrService;
import io.github.wimdeblauwe.ttcli.buildtools.BuildToolInitService;
import io.github.wimdeblauwe.ttcli.help.HelpTextInitService;
import io.github.wimdeblauwe.ttcli.java.JavaCodeInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceFactory;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindDependencyInitService;
import io.github.wimdeblauwe.ttcli.thymeleaf.ThymeleafTemplatesInitService;
import io.github.wimdeblauwe.ttcli.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class ProjectInitializationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectInitializationService.class);
    private final SpringBootInitializrService initializrService;
    private final JavaCodeInitService javaCodeInitService;
    private final LiveReloadInitServiceFactory liveReloadInitServiceFactory;
    private final ThymeleafTemplatesInitService thymeleafTemplatesInitService;
    private final List<BuildToolInitService> buildToolInitServices;
    private final TailwindDependencyInitService tailwindDependencyInitService;
    private final HelpTextInitService helpTextInitService;

    public ProjectInitializationService(SpringBootInitializrService initializrService,
                                        JavaCodeInitService javaCodeInitService,
                                        LiveReloadInitServiceFactory liveReloadInitServiceFactory,
                                        ThymeleafTemplatesInitService thymeleafTemplatesInitService,
                                        List<BuildToolInitService> buildToolInitServices,
                                        TailwindDependencyInitService tailwindDependencyInitService,
                                        HelpTextInitService helpTextInitService) {
        this.initializrService = initializrService;
        this.javaCodeInitService = javaCodeInitService;
        this.liveReloadInitServiceFactory = liveReloadInitServiceFactory;
        this.thymeleafTemplatesInitService = thymeleafTemplatesInitService;
        this.buildToolInitServices = buildToolInitServices;
        this.tailwindDependencyInitService = tailwindDependencyInitService;
        this.helpTextInitService = helpTextInitService;
    }

    public void initialize(ProjectInitializationParameters parameters) throws IOException, InterruptedException {
        Path basePath = parameters.basePath();
        if (!FileUtil.isEmpty(basePath)) {
            throw new ProjectInitializationServiceException("The directory is not empty! Unable to generate project in " + basePath.toAbsolutePath());
        }
        if (!Files.exists(basePath)) {
            Files.createDirectories(basePath);
        }

        LiveReloadInitService liveReloadInitService = liveReloadInitServiceFactory.getInitService(parameters.liveReloadInitServiceParameters().initServiceId(),
                parameters.tailwindVersion(),
                                                                                                  parameters.hasTailwindCssWebDependency());
        LOGGER.info("Using init service {}", liveReloadInitService);
        initializrService.generate(basePath,
                                   parameters.springBootProjectCreationParameters(),
                                   liveReloadInitService.additionalSpringInitializrDependencies());
        javaCodeInitService.generate(parameters);

        liveReloadInitService.generate(parameters);
        helpTextInitService.addHelpText(basePath, liveReloadInitService.getHelpText());

        BuildToolInitService buildToolInitService = buildToolInitServices.stream().filter(buildTool -> buildTool.canHandle(parameters)).findAny().orElseThrow();
        buildToolInitService.generate(parameters);
        tailwindDependencyInitService.generate(liveReloadInitService, parameters);
        thymeleafTemplatesInitService.generate(parameters);

        liveReloadInitService.runBuild(parameters);
    }
}
