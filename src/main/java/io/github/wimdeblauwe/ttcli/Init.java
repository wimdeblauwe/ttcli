package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.IdAndName;
import io.github.wimdeblauwe.ttcli.boot.SpringBootInitializrClient;
import io.github.wimdeblauwe.ttcli.boot.SpringBootProjectCreationParameters;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import io.github.wimdeblauwe.ttcli.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.component.flow.DefaultSelectItem;
import org.springframework.shell.component.flow.SelectItem;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ShellComponent
public class Init {
    private static final String CSS_FRAMEWORK_BOOTSTRAP = "bootstrap";
    private static final String CSS_FRAMEWORK_TAILWIND_CSS = "tailwindcss";
    @Autowired
    private ComponentFlow.Builder flowBuilder;
    @Autowired
    private List<WebDependency> webDependencies;
    @Autowired
    private SpringBootInitializrClient initializrClient;
    @Autowired
    private InitService initService;

    @ShellMethod
    public void init(@ShellOption(defaultValue = ".") String baseDir) throws IOException, InterruptedException {
        Path basePath = Path.of(baseDir);
        if (!FileUtil.isEmpty(basePath)) {
            System.out.println("The directory is not empty! Unable to generate project in " + basePath.toAbsolutePath());
            return;
        }

        List<IdAndName> springBootVersions = initializrClient.getMetadata().getBootVersion().getValues();
        ComponentFlow.Builder builder = flowBuilder.clone().reset();

        addGroupIdInput(builder);
        addArtifactIdInput(builder);
        addProjectNameInput(builder);
        addSpringBootVersionInput(builder, springBootVersions);
        addCssFrameworkInput(builder);
        addWebDependenciesInput(builder);

        ComponentFlow flow = builder.build();
        ComponentFlow.ComponentFlowResult flowResult = flow.run();

        ComponentContext<?> context = flowResult.getContext();
        String groupId = context.get("group-id");
        String artifactId = context.get("artifact-id");
        String projectName = context.get("project-name");
        String springBootVersion = context.get("spring-boot-version");

        initService.initialize(new InitParameters(basePath,
                                                  new SpringBootProjectCreationParameters(groupId,
                                                                                          artifactId,
                                                                                          projectName,
                                                                                          springBootVersion)));


        String cssFrameworkSelection = context.get("css-framework");
        List<String> selectedWebDependencyOptions = context.get("web-dependencies");
        List<WebDependency> selectedWebDependencies = webDependencies.stream().filter(webDependency -> selectedWebDependencyOptions.contains(webDependency.id())).toList();
        LiveReloadInitStrategy strategy;
        if (cssFrameworkSelection.equals(CSS_FRAMEWORK_BOOTSTRAP)) {
            System.out.println("\uD83D\uDC85 Going with Bootstrap");
            strategy = new BootstrapLiveReloadInitStrategy(selectedWebDependencies);
        } else if (cssFrameworkSelection.equals(CSS_FRAMEWORK_TAILWIND_CSS)) {
            System.out.println("\uD83D\uDC85 Going with Tailwind CSS");
            strategy = new TailwindCssLiveReloadInitStrategy(selectedWebDependencies);
        } else {
            throw new IllegalArgumentException("unknown css framework: " + cssFrameworkSelection);
        }

        strategy.execute(new LiveReloadInitParameters(basePath));

        System.out.println("✅ Done");
        System.exit(0);
    }

    private Map<String, String> convertToMap(List<IdAndName> springBootVersions) {
        return springBootVersions.stream().collect(Collectors.toMap(IdAndName::name, IdAndName::id));
    }

    private List<SelectItem> buildWebDependencyOptions() {
        return webDependencies.stream()
                              .map(webDependency -> new DefaultSelectItem(webDependency.displayName(), webDependency.id(), true, false))
                              .collect(Collectors.toList());
    }

    private void addGroupIdInput(ComponentFlow.Builder builder) {
        builder.withStringInput("group-id")
               .name("Group: ")
               .defaultValue("com.example")
               .and();
    }

    private void addArtifactIdInput(ComponentFlow.Builder builder) {
        builder.withStringInput("artifact-id")
               .name("Artifact: ")
               .defaultValue("demo")
               .and();
    }

    private void addProjectNameInput(ComponentFlow.Builder builder) {
        builder.withStringInput("project-name")
               .name("Project Name: ")
               .and();
    }

    private void addSpringBootVersionInput(ComponentFlow.Builder builder,
                                           List<IdAndName> springBootVersions) {
        builder.withSingleItemSelector("spring-boot-version")
               .name("Select Spring Boot version")
               .selectItems(convertToMap(springBootVersions))
               .and();
    }

    private void addWebDependenciesInput(ComponentFlow.Builder builder) {
        builder.withMultiItemSelector("web-dependencies")
               .name("Web dependencies")
               .selectItems(buildWebDependencyOptions())
               .and();
    }

    private void addCssFrameworkInput(ComponentFlow.Builder builder) {
        builder.withSingleItemSelector("css-framework")
               .name("CSS Framework to use")
               .selectItems(Map.of("Bootstrap", CSS_FRAMEWORK_BOOTSTRAP,
                                   "Tailwind CSS", CSS_FRAMEWORK_TAILWIND_CSS))
               .and();
    }
}
