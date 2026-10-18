package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.boot.*;
import io.github.wimdeblauwe.ttcli.deps.HtmxVersion;
import io.github.wimdeblauwe.ttcli.deps.HtmxWebDependency;
import io.github.wimdeblauwe.ttcli.deps.ShadleafWebDependency;
import io.github.wimdeblauwe.ttcli.deps.TailwindCssWebDependency;
import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceFactory;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceParameters;
import io.github.wimdeblauwe.ttcli.npm.PackageManager;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindDependency;
import io.github.wimdeblauwe.ttcli.tailwind.TailwindVersion;
import io.github.wimdeblauwe.ttcli.template.TemplateEngineType;
import io.github.wimdeblauwe.ttcli.util.ConsoleOutput;
import io.github.wimdeblauwe.ttcli.util.InetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.shell.jline.tui.component.context.ComponentContext;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;
import org.springframework.shell.jline.tui.component.flow.DefaultSelectItem;
import org.springframework.shell.jline.tui.component.flow.SelectItem;
import org.springframework.shell.jline.tui.component.support.Nameable;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Init {
    private static final Logger LOGGER = LoggerFactory.getLogger(Init.class);
    private static final String DEFAULT_GROUP = "com.example";
    private static final String DEFAULT_ARTIFACT = "demo";
    private static final String DEFAULT_NAME = "Demo";
    private static final String DEFAULT_LIVE_RELOAD = "vite";
    private static final String LATEST_LTS_JAVA_VERSION = "25";

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

    @Autowired
    private InitOptionsValidator optionsValidator;

    @Command(description = "Generate a Spring Boot project with Thymeleaf or JTE templating. "
            + "Every question can be answered with an option; use --non-interactive to never prompt.")
    public void init(@Option(defaultValue = ".", longName = "baseDir") String baseDir,
                     @Option(longName = "group", description = "Maven groupId (default: com.example)") String group,
                     @Option(longName = "artifact", description = "Maven artifactId (default: demo)") String artifact,
                     @Option(longName = "name", description = "Project name (default: Demo)") String name,
                     @Option(longName = "boot-version", description = "Spring Boot version, as offered by start.spring.io (default: start.spring.io default)") String bootVersion,
                     @Option(longName = "java-version", description = "Java version, as offered by start.spring.io (default: start.spring.io default)") String javaVersion,
                     @Option(longName = "template-engine", description = "thymeleaf or jte (default: thymeleaf)") String templateEngine,
                     @Option(longName = "thymeleaf-layout-dialect", description = "yes or no, Thymeleaf only (default: no)") String thymeleafLayoutDialect,
                     @Option(longName = "live-reload", description = "Live reload implementation: vite, npm-based or dev-tools-based (default when non-interactive: vite)") String liveReload,
                     @Option(longName = "package-manager", description = "npm or pnpm (default: npm)") String packageManager,
                     @Option(longName = "web-deps", description = "Comma separated web dependencies, or 'none' (default: none)") String webDeps,
                     @Option(longName = "htmx-version", description = "2 or 4 (default: 4, or 2 if htmx 4 is not supported)") String htmxVersion,
                     @Option(longName = "tailwind-version", description = "3 or 4 (default: 4)") String tailwindVersion,
                     @Option(longName = "tailwind-deps", description = "Comma separated Tailwind dependencies, or 'none' (default: none)") String tailwindDeps,
                     @Option(longName = "non-interactive", description = "Never prompt: use defaults for missing answers and fail when a required one is missing") boolean nonInteractive) throws IOException, InterruptedException {

        InitOptions options = new InitOptions(group, artifact, name, bootVersion, javaVersion, templateEngine,
                thymeleafLayoutDialect, liveReload, packageManager, webDeps, htmxVersion, tailwindVersion, tailwindDeps,
                nonInteractive);
        try {
            run(options, options.nonInteractive() || !hasTerminal(), baseDir);
        } catch (Exception e) {
            LOGGER.error("Error during project generation: " + e.getMessage(), e);
            ConsoleOutput.printlnError(ConsoleOutput.Icon.ERROR, "Error during project generation: " + e.getMessage());
            // Rethrow so the process exits with a non-zero code
            throw e;
        }
    }

    private static boolean hasTerminal() {
        Console console = System.console();
        return console != null && console.isTerminal();
    }

    private void run(InitOptions options, boolean nonInteractive, String baseDir) throws IOException, InterruptedException {
        InitializrMetadata metadata = loadMetadata();

        // Questions without an option (and not defaulted) go into one flow
        ComponentFlow.Builder builder = flowBuilder.clone().reset();
        int questions = 0;
        if (options.group() == null && !nonInteractive) {
            addGroupIdInput(builder);
            questions++;
        }
        if (options.artifact() == null && !nonInteractive) {
            addArtifactIdInput(builder);
            questions++;
        }
        if (options.name() == null && !nonInteractive) {
            addProjectNameInput(builder);
            questions++;
        }
        if (options.springBootVersion() == null && !nonInteractive) {
            addSpringBootVersionInput(builder, metadata);
            questions++;
        }
        if (options.javaVersion() == null && !nonInteractive) {
            addJavaVersionInput(builder, metadata);
            questions++;
        }
        if (options.templateEngine() == null && !nonInteractive) {
            addTemplateEngineInput(builder);
            questions++;
        }
        ComponentContext<?> context = questions > 0 ? builder.build().run().getContext() : null;

        String groupId = options.group() != null ? options.group()
                : context != null && context.get("group-id") != null ? context.get("group-id") : DEFAULT_GROUP;
        String artifactId = options.artifact() != null ? optionsValidator.validateArtifact(options.artifact())
                : context != null && context.get("artifact-id") != null ? context.get("artifact-id") : DEFAULT_ARTIFACT;
        String projectName = options.name() != null ? options.name()
                : context != null && context.get("project-name") != null ? context.get("project-name") : DEFAULT_NAME;
        String springBootVersion = options.springBootVersion() != null
                ? optionsValidator.validateInitializrValue("--boot-version", options.springBootVersion(), metadata.getBootVersion())
                : context != null && context.get("spring-boot-version") != null ? context.get("spring-boot-version") : metadata.getBootVersion().getDefaultId();
        String javaVersion = options.javaVersion() != null
                ? optionsValidator.validateInitializrValue("--java-version", options.javaVersion(), metadata.getJavaVersion())
                : context != null && context.get("java-version") != null ? context.get("java-version") : defaultJavaVersion(metadata).id();
        String templateEngineId = options.templateEngine() != null
                ? optionsValidator.validateTemplateEngine(options.templateEngine())
                : context != null && context.get("template-engine") != null ? context.get("template-engine") : TemplateEngineType.TYPE_THYMELEAF;

        TemplateEngineType templateEngineType = getTemplateEngineType(templateEngineId, options.thymeleafLayoutDialect(), nonInteractive);
        String liveReloadId = resolveLiveReload(options.liveReload(), templateEngineType, nonInteractive);

        // npm-based and vite always need a package manager — ask right after live-reload.
        // dev-tools-based only needs one if Tailwind is selected, so defer until web deps are known.
        PackageManager packageManagerEarly = liveReloadAlwaysUsesNode(liveReloadId)
                ? resolvePackageManager(options.packageManager(), true, liveReloadId, nonInteractive)
                : null;

        List<WebDependency> selectedWebDependencies = resolveWebDependencies(options.webDeps(), springBootVersion, templateEngineType, nonInteractive);
        selectedWebDependencies = allowHtmxVersionSelection(selectedWebDependencies, options.htmxVersion(), springBootVersion, templateEngineType, nonInteractive);
        boolean hasShadleafWebDependency = selectedWebDependencies.stream().anyMatch(webDependency -> webDependency instanceof ShadleafWebDependency);
        List<WebDependency> webDependenciesWithoutBundledAlpine = ShadleafWebDependency.removeAlpineIfShadleafIsSelected(selectedWebDependencies);
        if (webDependenciesWithoutBundledAlpine.size() < selectedWebDependencies.size()) {
            ConsoleOutput.println(ConsoleOutput.Icon.INFO, "Alpine.js is provided by Shadleaf, not adding it separately.");
        }
        selectedWebDependencies = webDependenciesWithoutBundledAlpine;

        boolean hasTailwindCssWebDependency = selectedWebDependencies.stream().anyMatch(webDependency -> webDependency instanceof TailwindCssWebDependency);
        PackageManager packageManager = packageManagerEarly != null
                ? packageManagerEarly
                : resolvePackageManager(options.packageManager(), liveReloadUsesNode(liveReloadId, hasTailwindCssWebDependency), liveReloadId, nonInteractive);
        TailwindVersion tailwindVersion = allowTailwindVersionSelection(options.tailwindVersion(), hasTailwindCssWebDependency, hasShadleafWebDependency, nonInteractive).orElse(null);
        List<TailwindDependency> selectedTailwindDependencies = allowTailwindDependenciesSelection(options.tailwindDeps(), hasTailwindCssWebDependency, nonInteractive);

        Path basePath = Path.of(baseDir).resolve(artifactId);

        printSummary(basePath, groupId, artifactId, projectName, springBootVersion, javaVersion, templateEngineType, liveReloadId,
                packageManager, selectedWebDependencies, tailwindVersion, selectedTailwindDependencies);

        projectInitializationService.initialize(new ProjectInitializationParameters(basePath,
                new SpringBootProjectCreationParameters(groupId,
                        artifactId,
                        projectName,
                        springBootVersion,
                        javaVersion),
                new LiveReloadInitServiceParameters(liveReloadId, packageManager),
                selectedWebDependencies,
                tailwindVersion,
                selectedTailwindDependencies,
                templateEngineType));

        ConsoleOutput.println(ConsoleOutput.Icon.SUCCESS, "Done generating project at " + basePath.toAbsolutePath());
        System.out.println();
        System.out.println("See HELP.md in the generated project for additional information.");
    }

    private void printSummary(Path basePath,
                              String groupId,
                              String artifactId,
                              String projectName,
                              String springBootVersion,
                              String javaVersion,
                              TemplateEngineType templateEngineType,
                              String liveReloadId,
                              PackageManager packageManager,
                              List<WebDependency> webDependencies,
                              TailwindVersion tailwindVersion,
                              List<TailwindDependency> tailwindDependencies) {
        ConsoleOutput.println(ConsoleOutput.Icon.CONFIGURE, "Generating project with these answers:");
        System.out.println("  Directory:           " + basePath.toAbsolutePath());
        System.out.println("  Group:               " + groupId);
        System.out.println("  Artifact:            " + artifactId);
        System.out.println("  Project name:        " + projectName);
        System.out.println("  Spring Boot version: " + springBootVersion);
        System.out.println("  Java version:        " + javaVersion);
        System.out.println("  Template engine:     " + templateEngineType.id());
        if (templateEngineType instanceof TemplateEngineType.Thymeleaf thymeleaf) {
            System.out.println("  Layout dialect:      " + (thymeleaf.useLayoutDialect() ? "yes" : "no"));
        }
        System.out.println("  Live reload:         " + liveReloadId);
        System.out.println("  Package manager:     " + packageManager.executable());
        System.out.println("  Web dependencies:    " + joinOrNone(webDependencies.stream()
                .map(d -> d instanceof HtmxWebDependency htmx ? d.id() + " " + htmx.htmxVersion().name().substring(8) : d.id()).toList()));
        if (tailwindVersion != null) {
            System.out.println("  Tailwind version:    " + tailwindVersion.name().substring(8));
            System.out.println("  Tailwind deps:       " + joinOrNone(tailwindDependencies.stream().map(TailwindDependency::id).toList()));
        }
        System.out.println();
    }

    private static String joinOrNone(List<String> values) {
        return values.isEmpty() ? "none" : String.join(", ", values);
    }

    private InitializrMetadata loadMetadata() {
        try {
            InetUtil.checkIfInternetAccessIsAvailable("start.spring.io");
            return initializrClient.getMetadata();
        } catch (IllegalStateException e) {
            throw new ProjectInitializationServiceException(e.getMessage());
        }
    }

    private String resolveLiveReload(String optionValue, TemplateEngineType templateEngineType, boolean nonInteractive) {
        if (optionValue != null) {
            return optionsValidator.validateLiveReload(optionValue, templateEngineType);
        }
        if (nonInteractive) {
            return DEFAULT_LIVE_RELOAD;
        }
        ComponentFlow.Builder builder = flowBuilder.clone().reset();
        addLiveReloadInput(builder, templateEngineType);
        return builder.build().run().getContext().get("live-reload");
    }

    private List<WebDependency> resolveWebDependencies(String optionValue,
                                                       String springBootVersion,
                                                       TemplateEngineType templateEngineType,
                                                       boolean nonInteractive) {
        if (optionValue != null) {
            return optionsValidator.validateWebDependencies(optionValue, springBootVersion, templateEngineType);
        }
        if (nonInteractive) {
            return List.of();
        }
        ComponentFlow.Builder builder = flowBuilder.clone().reset();
        addWebDependenciesInput(builder, springBootVersion, templateEngineType);
        ComponentContext<?> context = builder.build().run().getContext();
        List<String> selectedWebDependencyOptions = context.get("web-dependencies");
        return webDependencies.stream().filter(webDependency -> selectedWebDependencyOptions.contains(webDependency.id())).toList();
    }

    private TemplateEngineType getTemplateEngineType(String templateEngineId, String layoutDialectOption, boolean nonInteractive) {
        TemplateEngineType templateEngineType;
        if (templateEngineId.equals(TemplateEngineType.TYPE_THYMELEAF)) {
            boolean useThymeleafLayoutDialect;
            if (layoutDialectOption != null) {
                useThymeleafLayoutDialect = optionsValidator.validateLayoutDialect(layoutDialectOption, templateEngineId);
            } else if (nonInteractive) {
                useThymeleafLayoutDialect = false;
            } else {
                ComponentFlow.Builder builder = flowBuilder.clone().reset();
                builder.withSingleItemSelector("thymeleaf-layout-dialect")
                        .name("Use Thymeleaf Layout Dialect?")
                        .selectItems(List.of(new DefaultSelectItem("Yes", "true", true, false), new DefaultSelectItem("No", "false", true, false)))
                        .defaultSelect("No")
                        .and();
                ComponentFlow flow = builder.build();
                ComponentFlow.ComponentFlowResult flowResult = flow.run();
                ComponentContext<?> context = flowResult.getContext();
                useThymeleafLayoutDialect = Boolean.parseBoolean(context.get("thymeleaf-layout-dialect"));
            }
            templateEngineType = new TemplateEngineType.Thymeleaf(useThymeleafLayoutDialect);
        } else if (templateEngineId.equals(TemplateEngineType.TYPE_JTE)) {
            if (layoutDialectOption != null) {
                optionsValidator.validateLayoutDialect(layoutDialectOption, templateEngineId); // always throws for JTE
            }
            templateEngineType = new TemplateEngineType.Jte();
        } else {
            throw new ProjectInitializationServiceException("Invalid template engine: " + templateEngineId);
        }
        return templateEngineType;
    }

    private PackageManager resolvePackageManager(String optionValue,
                                                 boolean packageManagerUsed,
                                                 String liveReloadId,
                                                 boolean nonInteractive) {
        if (optionValue != null) {
            return optionsValidator.validatePackageManager(optionValue, packageManagerUsed, liveReloadId);
        }
        if (!packageManagerUsed || nonInteractive) {
            return PackageManager.NPM;
        }
        return promptPackageManager();
    }

    private PackageManager promptPackageManager() {
        ComponentFlow.Builder builder = flowBuilder.clone().reset();
        builder.withSingleItemSelector("package-manager")
                .name("Select package manager")
                .selectItems(Map.of("npm", PackageManager.NPM.name(),
                        "pnpm", PackageManager.PNPM.name()))
                .defaultSelect("npm")
                .and();
        ComponentFlow.ComponentFlowResult flowResult = builder.build().run();
        return PackageManager.valueOf(flowResult.getContext().get("package-manager"));
    }

    private boolean liveReloadAlwaysUsesNode(String liveReloadId) {
        return "npm-based".equals(liveReloadId) || "vite".equals(liveReloadId);
    }

    private boolean liveReloadUsesNode(String liveReloadId, boolean hasTailwindCssWebDependency) {
        return switch (liveReloadId) {
            case "npm-based", "vite" -> true;
            case "dev-tools-based" -> hasTailwindCssWebDependency;
            default -> false;
        };
    }

    private List<WebDependency> allowHtmxVersionSelection(List<WebDependency> selectedWebDependencies,
                                                          String optionValue,
                                                          String springBootVersion,
                                                          TemplateEngineType templateEngineType,
                                                          boolean nonInteractive) {
        if (selectedWebDependencies.stream().noneMatch(webDependency -> webDependency instanceof HtmxWebDependency)) {
            if (optionValue != null) {
                optionsValidator.validateHtmxVersion(optionValue, selectedWebDependencies, springBootVersion, templateEngineType); // always throws
            }
            return selectedWebDependencies;
        }

        HtmxVersion htmxVersion;
        if (optionValue != null) {
            htmxVersion = optionsValidator.validateHtmxVersion(optionValue, selectedWebDependencies, springBootVersion, templateEngineType);
        } else if (!HtmxWebDependency.supportsHtmx4(springBootVersion, templateEngineType)) {
            htmxVersion = HtmxVersion.VERSION_2;
        } else if (nonInteractive) {
            htmxVersion = HtmxVersion.VERSION_4;
        } else {
            ComponentFlow.Builder builder = flowBuilder.clone().reset();
            addHtmxVersionInput(builder);
            ComponentFlow.ComponentFlowResult flowResult = builder.build().run();
            htmxVersion = HtmxVersion.valueOf(flowResult.getContext().get("htmx-version"));
        }

        return selectedWebDependencies.stream()
                .map(webDependency -> webDependency instanceof HtmxWebDependency htmxWebDependency
                        ? htmxWebDependency.withVersion(htmxVersion)
                        : webDependency)
                .toList();
    }

    private Optional<TailwindVersion> allowTailwindVersionSelection(String optionValue,
                                                                    boolean hasTailwindCssWebDependency,
                                                                    boolean hasShadleafWebDependency,
                                                                    boolean nonInteractive) {
        if (optionValue != null) {
            return Optional.of(optionsValidator.validateTailwindVersion(optionValue, hasTailwindCssWebDependency, hasShadleafWebDependency));
        } else if (hasTailwindCssWebDependency && hasShadleafWebDependency) {
            // Shadleaf's embedded bundle needs Tailwind 4
            return Optional.of(TailwindVersion.VERSION_4);
        } else if (hasTailwindCssWebDependency && nonInteractive) {
            return Optional.of(TailwindVersion.VERSION_4);
        } else if (hasTailwindCssWebDependency) {
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

    private List<TailwindDependency> allowTailwindDependenciesSelection(String optionValue,
                                                                        boolean hasTailwindCssWebDependency,
                                                                        boolean nonInteractive) {
        if (optionValue != null) {
            return optionsValidator.validateTailwindDependencies(optionValue, hasTailwindCssWebDependency);
        } else if (hasTailwindCssWebDependency && !nonInteractive) {
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

    private void addSpringBootVersionInput(ComponentFlow.Builder builder, InitializrMetadata metadata) {
        SpringInitializrSingleSelect bootVersion = metadata.getBootVersion();
        builder.withSingleItemSelector("spring-boot-version")
                .name("Select Spring Boot version")
                .selectItems(convertToMap(bootVersion.getValues()))
                .sort(Comparator.comparing(Nameable::getName, Comparator.naturalOrder()))
                .defaultSelect(bootVersion.getDefaultName())
                .and();
    }

    /**
     * The latest LTS if start.spring.io offers it, otherwise the start.spring.io default.
     */
    private IdAndName defaultJavaVersion(InitializrMetadata metadata) {
        SpringInitializrSingleSelect javaVersion = metadata.getJavaVersion();
        return javaVersion.getValues().stream()
                .filter(v -> v.id().equals(LATEST_LTS_JAVA_VERSION))
                .findFirst()
                .orElseGet(() -> javaVersion.getValues().stream()
                        .filter(v -> v.id().equals(javaVersion.getDefaultId()))
                        .findFirst().orElseThrow());
    }

    private void addJavaVersionInput(ComponentFlow.Builder builder, InitializrMetadata metadata) {
        builder.withSingleItemSelector("java-version")
                .name("Java version")
                .selectItems(convertToMap(metadata.getJavaVersion().getValues()))
                .sort(Comparator.comparing(Nameable::getName, Comparator.naturalOrder()))
                .defaultSelect(defaultJavaVersion(metadata).name())
                .and();
    }

    private void addLiveReloadInput(ComponentFlow.Builder builder, TemplateEngineType templateEngineType) {
        Map<String, String> reloadOptions = new HashMap<>();
        List<String> reloadOptionIdsInOrder = new ArrayList<>();
        for (LiveReloadInitService initService : liveReloadInitServiceFactory.getNormalServices()) {
            if (initService.isApplicableForTemplateEngine(templateEngineType)) {
                reloadOptions.put(initService.getName(), initService.getId());
                reloadOptionIdsInOrder.add(initService.getId());
            }
        }
        builder.withSingleItemSelector("live-reload")
                .sort(Comparator.comparingInt(o -> reloadOptionIdsInOrder.indexOf(o.getItem())))
                .name("Select live reload implementation:")
                .selectItems(reloadOptions)
                .max(reloadOptions.size())
                .and();
    }

    private void addTemplateEngineInput(ComponentFlow.Builder builder) {
        Map<String, String> templateEngineOptions = new HashMap<>();
        templateEngineOptions.put("Thymeleaf", TemplateEngineType.TYPE_THYMELEAF);
        templateEngineOptions.put("JTE", TemplateEngineType.TYPE_JTE);

        builder.withSingleItemSelector("template-engine")
                .name("Select template engine")
                .selectItems(templateEngineOptions)
                .defaultSelect("Thymeleaf")
                .and();
    }

    private void addWebDependenciesInput(ComponentFlow.Builder builder,
                                         String springBootVersion,
                                         TemplateEngineType templateEngineType) {
        List<SelectItem> webDependencyOptions = buildWebDependencyOptions(springBootVersion, templateEngineType);
        builder.withMultiItemSelector("web-dependencies")
                .name("Web dependencies")
                .selectItems(webDependencyOptions)
                .max(webDependencyOptions.size())
                .and();
    }

    private List<SelectItem> buildWebDependencyOptions(String springBootVersion,
                                                       TemplateEngineType templateEngineType) {
        return webDependencies.stream()
                .filter(webDependency -> webDependency.isApplicable(springBootVersion, templateEngineType))
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

    private void addHtmxVersionInput(ComponentFlow.Builder builder) {
        builder.withSingleItemSelector("htmx-version")
                .name("htmx version")
                .selectItems(Map.of(
                        "htmx 2", HtmxVersion.VERSION_2.name(),
                        "htmx 4", HtmxVersion.VERSION_4.name())
                )
                .defaultSelect("htmx 4")
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
