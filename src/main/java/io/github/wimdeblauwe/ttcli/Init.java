package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.*;
import io.github.wimdeblauwe.ttcli.deps.TailwindCssWebDependency;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceFactory;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceParameters;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindDependency;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindVersion;
import io.github.wimdeblauwe.ttcli.util.InetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.component.flow.DefaultSelectItem;
import org.springframework.shell.component.flow.SelectItem;
import org.springframework.shell.component.support.Nameable;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@ShellComponent
public class Init {
    private static final Logger LOGGER = LoggerFactory.getLogger(Init.class);

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
    public void init(@ShellOption(defaultValue = ".", value = "baseDir") String baseDir) throws IOException, InterruptedException {

        try {
            ComponentFlow.Builder builder = flowBuilder.clone().reset();
            addProjectTypeInput(builder);
            addGroupIdInput(builder);
            addArtifactIdInput(builder);
            addProjectNameInput(builder);
            addSpringBootVersionInput(builder);
            addLiveReloadInput(builder);
            addWebDependenciesInput(builder);

            ComponentFlow flow = builder.build();
            ComponentFlow.ComponentFlowResult flowResult = flow.run();

            ComponentContext<?> context = flowResult.getContext();
            String projectType = context.get("project-type");
            String groupId = context.get("group-id");
            String artifactId = context.get("artifact-id");
            String projectName = context.get("project-name");
            String springBootVersion = context.get("spring-boot-version");
            String javaVersion = context.get("java-version");

            List<String> selectedWebDependencyOptions = context.get("web-dependencies");
            List<WebDependency> selectedWebDependencies = webDependencies.stream().filter(webDependency -> selectedWebDependencyOptions.contains(webDependency.id())).toList();

            boolean hasTailwindCssWebDependency = selectedWebDependencies.stream().anyMatch(webDependency -> webDependency instanceof TailwindCssWebDependency);
            TailwindVersion tailwindVersion = allowTailwindVersionSelection(hasTailwindCssWebDependency).orElse(null);
            List<TailwindDependency> selectedTailwindDependencies = allowTailwindDependenciesSelection(hasTailwindCssWebDependency);

            Path basePath = Path.of(baseDir).resolve(artifactId);

            projectInitializationService.initialize(new ProjectInitializationParameters(basePath,
                    new SpringBootProjectCreationParameters(
                            SpringBootProjectType.valueOf(projectType),
                            groupId,
                            artifactId,
                            projectName,
                            springBootVersion,
                            javaVersion),
                    new LiveReloadInitServiceParameters(context.get("live-reload")),
                    selectedWebDependencies,
                    tailwindVersion,
                    selectedTailwindDependencies));

            System.out.println("✅ Done generating project at " + basePath.toAbsolutePath());
            System.out.println();
            System.out.println("See HELP.md in the generated project for additional information.");
        } catch (Exception e) {
            LOGGER.error("Error during project generation: " + e.getMessage(), e);
            System.err.println("❌ Error during project generation: " + e.getMessage());
        }
    }

    private Optional<TailwindVersion> allowTailwindVersionSelection(boolean hasTailwindCssWebDependency) {
        if (hasTailwindCssWebDependency) {
            ComponentFlow.Builder builder = flowBuilder.clone().reset();
            addTailwindVersionInput(builder);
            ComponentFlow build = builder.build();
            ComponentFlow.ComponentFlowResult flowResult = build.run();
            ComponentContext<?> context = flowResult.getContext();
            String selectedTailwindVersion = context.get("tailwind-version");
            return Optional.of(TailwindVersion.valueOf(selectedTailwindVersion));
        } else {
            return Optional.empty();
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

    private void addProjectTypeInput(ComponentFlow.Builder builder){
        Map<String, String> typeOptions = new HashMap<>();
        for (SpringBootProjectType type : SpringBootProjectType.values()) {
            typeOptions.put(type.description(), type.name());
        }
        builder.withSingleItemSelector("project-type")
                .name("Select Spring Boot project type:")
                .selectItems(typeOptions)
                .max(typeOptions.size())
                .and();
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
            SpringInitializrSingleSelect bootVersion = metadata.getBootVersion();
            List<IdAndName> springBootVersions = bootVersion.getValues();

            builder.withSingleItemSelector("spring-boot-version")
                    .name("Select Spring Boot version")
                    .selectItems(convertToMap(springBootVersions))
                    .sort(Comparator.comparing(Nameable::getName, Comparator.naturalOrder()))
                    .defaultSelect(bootVersion.getDefaultName())
                    .and()
                    .withSingleItemSelector("java-version")
                    .name("Java version")
                    .selectItems(convertToMap(metadata.getJavaVersion().getValues()))
                    .sort(Comparator.comparing(Nameable::getName, Comparator.naturalOrder()))
                    .defaultSelect(metadata.getJavaVersion().getDefaultName())
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

    private void addTailwindVersionInput(ComponentFlow.Builder builder) {
        builder.withSingleItemSelector("tailwind-version")
                .name("Tailwind version")
                .selectItems(Map.of(
                        "Tailwind 3", TailwindVersion.VERSION_3.name(),
                        "Tailwind 4", TailwindVersion.VERSION_4.name())
                )
                .defaultSelect("Tailwind 4")
                .and();
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
