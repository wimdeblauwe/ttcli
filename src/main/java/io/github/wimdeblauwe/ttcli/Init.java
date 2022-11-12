package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.*;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceFactory;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceParameters;
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
import java.util.*;
import java.util.stream.Collectors;

@ShellComponent
public class Init {
    @Autowired
    private ComponentFlow.Builder flowBuilder;
    @Autowired
    private List<WebDependency> webDependencies;
    @Autowired
    private SpringBootInitializrClient initializrClient;
    @Autowired
    private ProjectInitializationService projectInitializationService;
    @Autowired
    private LiveReloadInitServiceFactory liveReloadInitServiceFactory;

    @ShellMethod
    public void init(@ShellOption(defaultValue = ".") String baseDir) throws IOException {
        Path basePath = Path.of(baseDir);
        if (!FileUtil.isEmpty(basePath)) {
            System.out.println("The directory is not empty! Unable to generate project in " + basePath.toAbsolutePath());
            return;
        }

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

        projectInitializationService.initialize(new ProjectInitializationParameters(basePath,
                                                                                    new SpringBootProjectCreationParameters(groupId,
                                                                                                                            artifactId,
                                                                                                                            projectName,
                                                                                                                            springBootVersion),
                                                                                    new LiveReloadInitServiceParameters(context.get("live-reload")),
                                                                                    selectedWebDependencies));

        System.out.println("✅ Done");
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
               .defaultValue("Demo")
               .and();
    }

    private void addSpringBootVersionInput(ComponentFlow.Builder builder) {
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
    }

    private void addLiveReloadInput(ComponentFlow.Builder builder) {
        Map<String, String> reloadOptions = new HashMap<>();
        List<String> reloadOptionIdsInOrder = new ArrayList<>();
        for (LiveReloadInitService initService : liveReloadInitServiceFactory.getInitServices()) {
            reloadOptions.put(initService.getName(), initService.getId());
            reloadOptionIdsInOrder.add(initService.getId());
        }
        builder.withSingleItemSelector("live-reload")
               .sort(Comparator.comparingInt(o -> reloadOptionIdsInOrder.indexOf(o.getItem())))
               .name("Select live reload implementation:")
               .selectItems(reloadOptions)
               .and();
    }

    private void addWebDependenciesInput(ComponentFlow.Builder builder) {
        builder.withMultiItemSelector("web-dependencies")
               .name("Web dependencies")
               .selectItems(buildWebDependencyOptions())
               .and();
    }
}
