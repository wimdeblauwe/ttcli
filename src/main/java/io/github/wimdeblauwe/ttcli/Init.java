package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.*;
import io.github.wimdeblauwe.ttcli.deps.TailwindCssWebDependency;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceFactory;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceParameters;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindDependency;
import io.github.wimdeblauwe.ttcli.util.InetUtil;
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
import java.util.*;
import java.util.stream.Collectors;

@ShellComponent
public class Init {
    @Autowired
    private ComponentFlow.Builder flowBuilder;
    @Autowired
    private List<WebDependency> webDependencies;
    @Autowired
    private List<TailwindDependency> tailwindDependencies;
    @Autowired
    private SpringBootInitializrClient initializrClient;
    @Autowired
    private ProjectInitializationService projectInitializationService;
    @Autowired
    private LiveReloadInitServiceFactory liveReloadInitServiceFactory;

    @ShellMethod
    public void init(@ShellOption(defaultValue = ".") String baseDir) throws IOException {

        try {
            ComponentFlow.Builder builder = flowBuilder.clone().reset();

            addGroupIdInput(builder);
            addArtifactIdInput(builder);
            addProjectNameInput(builder);
            addSpringBootVersionInput(builder);
            addLiveReloadInput(builder);
            addWebDependenciesInput(builder);

            ComponentFlow flow = builder.build();
            ComponentFlow.ComponentFlowResult flowResult = flow.run();

            ComponentContext<?> context = flowResult.getContext();
            String groupId = context.get("group-id");
            String artifactId = context.get("artifact-id");
            String projectName = context.get("project-name");
            String springBootVersion = context.get("spring-boot-version");

            List<String> selectedWebDependencyOptions = context.get("web-dependencies");
            List<WebDependency> selectedWebDependencies = webDependencies.stream().filter(webDependency -> selectedWebDependencyOptions.contains(webDependency.id())).toList();

            boolean hasTailwindCssWebDependency = webDependencies.stream().anyMatch(webDependency -> webDependency instanceof TailwindCssWebDependency);
            List<TailwindDependency> selectedTailwindDependencies = allowTailwindDependenciesSelection(hasTailwindCssWebDependency);

            Path basePath = Path.of(baseDir).resolve(artifactId);

            projectInitializationService.initialize(new ProjectInitializationParameters(basePath,
                                                                                        new SpringBootProjectCreationParameters(groupId,
                                                                                                                                artifactId,
                                                                                                                                projectName,
                                                                                                                                springBootVersion),
                                                                                        new LiveReloadInitServiceParameters(context.get("live-reload")),
                                                                                        selectedWebDependencies,
                                                                                        selectedTailwindDependencies));

            System.out.println("✅ Done generating project at " + basePath.toAbsolutePath());
            System.out.println();
            System.out.println("See HELP.md in the generated project for additional information.");
        } catch (IOException | ProjectInitializationServiceException e) {
            System.err.println("❌ Error during project generation: " + e.getMessage());
        }
    }

    private List<TailwindDependency> allowTailwindDependenciesSelection(boolean hasTailwindCssWebDependency) {
        if (hasTailwindCssWebDependency) {
            ComponentFlow.Builder builder = flowBuilder.clone().reset();
            addTailwindDependenciesInput(builder);
            ComponentFlow build = builder.build();
            ComponentFlow.ComponentFlowResult flowResult = build.run();
            ComponentContext<?> context = flowResult.getContext();
            List<String> selectedTailwindDependencyOptions = context.get("tailwind-dependencies");
            return tailwindDependencies.stream()
                                       .filter(webDependency -> selectedTailwindDependencyOptions.contains(webDependency.id()))
                                       .toList();
        } else {
            return Collections.emptyList();
        }
    }

    private Map<String, String> convertToMap(List<IdAndName> springBootVersions) {
        return springBootVersions.stream().collect(Collectors.toMap(IdAndName::name, IdAndName::id));
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
               .defaultValue("Demo")
               .and();
    }

    private void addSpringBootVersionInput(ComponentFlow.Builder builder) {
        try {
            InetUtil.checkIfInternetAccessIsAvailable("start.spring.io");
            InitializrMetadata metadata = initializrClient.getMetadata();
            BootVersion bootVersion = metadata.getBootVersion();
            List<IdAndName> springBootVersions = bootVersion.getValues();

            Map<String, String> selectItems = convertToMap(springBootVersions);
            String defaultName = bootVersion.getDefaultName();
            builder.withSingleItemSelector("spring-boot-version")
                   .name("Select Spring Boot version")
                   .selectItems(selectItems)
                   .defaultSelect(defaultName)
                   .and();
        } catch (IllegalStateException e) {
            throw new ProjectInitializationServiceException(e.getMessage());
        }
    }

    private void addLiveReloadInput(ComponentFlow.Builder builder) {
        Map<String, String> reloadOptions = new HashMap<>();
        List<String> reloadOptionIdsInOrder = new ArrayList<>();
        for (LiveReloadInitService initService : liveReloadInitServiceFactory.getNormalServices()) {
            reloadOptions.put(initService.getName(), initService.getId());
            reloadOptionIdsInOrder.add(initService.getId());
        }
        builder.withSingleItemSelector("live-reload")
               .sort(Comparator.comparingInt(o -> reloadOptionIdsInOrder.indexOf(o.getItem())))
               .name("Select live reload implementation:")
               .selectItems(reloadOptions)
               .max(reloadOptions.size())
               .and();
    }

    private void addWebDependenciesInput(ComponentFlow.Builder builder) {
        builder.withMultiItemSelector("web-dependencies")
               .name("Web dependencies")
               .selectItems(buildWebDependencyOptions())
               .and();
    }

    private List<SelectItem> buildWebDependencyOptions() {
        return webDependencies.stream()
                              .map(webDependency -> new DefaultSelectItem(webDependency.displayName(), webDependency.id(), true, false))
                              .sorted(Comparator.comparing(DefaultSelectItem::name))
                              .collect(Collectors.toList());
    }

    private void addTailwindDependenciesInput(ComponentFlow.Builder builder) {
        builder.withMultiItemSelector("tailwind-dependencies")
               .name("Tailwind dependencies")
               .selectItems(buildTailwindDependencyOptions())
               .and();
    }

    private List<SelectItem> buildTailwindDependencyOptions() {
        return tailwindDependencies.stream()
                                   .map(tailwindDependency -> new DefaultSelectItem(tailwindDependency.displayName(), tailwindDependency.id(), true, false))
                                   .collect(Collectors.toList());
    }
}
