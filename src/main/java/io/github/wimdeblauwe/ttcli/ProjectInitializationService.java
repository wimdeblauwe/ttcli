package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.SpringBootInitializrService;
import io.github.wimdeblauwe.ttcli.help.HelpTextInitService;
import io.github.wimdeblauwe.ttcli.java.JavaCodeInitService;
import io.github.wimdeblauwe.ttcli.jte.JteTemplatesInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceFactory;
import io.github.wimdeblauwe.ttcli.maven.MavenInitService;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindDependencyInitService;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import io.github.wimdeblauwe.ttcli.thymeleaf.ThymeleafTemplatesInitService;
import io.github.wimdeblauwe.ttcli.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ProjectInitializationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectInitializationService.class);
    private final SpringBootInitializrService initializrService;
    private final JavaCodeInitService javaCodeInitService;
    private final LiveReloadInitServiceFactory liveReloadInitServiceFactory;
    private final ThymeleafTemplatesInitService thymeleafTemplatesInitService;
    private final JteTemplatesInitService jteTemplatesInitService;
    private final MavenInitService mavenInitService;
    private final TailwindDependencyInitService tailwindDependencyInitService;
    private final HelpTextInitService helpTextInitService;

    public ProjectInitializationService(SpringBootInitializrService initializrService,
                                        JavaCodeInitService javaCodeInitService,
                                        LiveReloadInitServiceFactory liveReloadInitServiceFactory,
                                        ThymeleafTemplatesInitService thymeleafTemplatesInitService,
                                        JteTemplatesInitService jteTemplatesInitService,
                                        MavenInitService mavenInitService,
                                        TailwindDependencyInitService tailwindDependencyInitService,
                                        HelpTextInitService helpTextInitService) {
        this.initializrService = initializrService;
        this.javaCodeInitService = javaCodeInitService;
        this.liveReloadInitServiceFactory = liveReloadInitServiceFactory;
        this.thymeleafTemplatesInitService = thymeleafTemplatesInitService;
        this.jteTemplatesInitService = jteTemplatesInitService;
        this.mavenInitService = mavenInitService;
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
                liveReloadInitService.additionalSpringInitializrDependencies(),
                parameters.templateEngineType());
        javaCodeInitService.generate(parameters);

        liveReloadInitService.generate(parameters);
        helpTextInitService.addHelpText(basePath, liveReloadInitService.getHelpText());

        mavenInitService.generate(parameters);
        tailwindDependencyInitService.generate(liveReloadInitService, parameters);

        // Generate templates based on the selected template engine
        if (parameters.templateEngineType() == TemplateEngineType.THYMELEAF) {
            thymeleafTemplatesInitService.generate(parameters);
        } else if (parameters.templateEngineType() == TemplateEngineType.JTE) {
            jteTemplatesInitService.generate(parameters);
        }

        liveReloadInitService.runBuild(parameters);
    }
}
