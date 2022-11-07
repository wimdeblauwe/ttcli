package io.github.wimdeblauwe.ttcli;

import io.github.wimdeblauwe.ttcli.deps.WebDependency;
import org.springframework.beans.factory.annotation.Autowired;
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
public class LiveReloadInit {
    private static final String CSS_FRAMEWORK_BOOTSTRAP = "bootstrap";
    private static final String CSS_FRAMEWORK_TAILWIND_CSS = "tailwindcss";
    @Autowired
    private ComponentFlow.Builder flowBuilder;
    @Autowired
    private List<WebDependency> webDependencies;

    @ShellMethod
    public void liveReloadInit(@ShellOption(defaultValue = ".") String baseDir) throws IOException, InterruptedException {
        ComponentFlow flow = flowBuilder.clone().reset()
                                        .withSingleItemSelector("css-framework")
                                        .name("CSS Framework to use")
                                        .selectItems(Map.of("Bootstrap", CSS_FRAMEWORK_BOOTSTRAP,
                                                            "Tailwind CSS", CSS_FRAMEWORK_TAILWIND_CSS))
                                        .and()
                                        .withMultiItemSelector("web-dependencies")
                                        .name("Web dependencies")
                                        .selectItems(buildWebDependencyOptions())
                                        .and()
                                        .build();
        ComponentFlow.ComponentFlowResult flowResult = flow.run();
        String cssFrameworkSelection = flowResult.getContext().get("css-framework");
        List<String> selectedWebDependencyOptions = flowResult.getContext().get("web-dependencies");
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

        strategy.execute(new LiveReloadInitParameters(Path.of(baseDir)));
    }

    private List<SelectItem> buildWebDependencyOptions() {
        return webDependencies.stream()
                              .map(webDependency -> new DefaultSelectItem(webDependency.displayName(), webDependency.id(), true, false))
                              .collect(Collectors.toList());
    }

}
