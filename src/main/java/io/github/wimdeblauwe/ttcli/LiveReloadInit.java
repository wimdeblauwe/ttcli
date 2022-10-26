package io.github.wimdeblauwe.ttcli;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.xmlbeam.XBProjector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@ShellComponent
public class LiveReloadInit {
    private static final String CSS_FRAMEWORK_BOOTSTRAP = "bootstrap";
    private static final String CSS_FRAMEWORK_TAILWIND_CSS = "tailwindcss";
    @Autowired
    private ComponentFlow.Builder flowBuilder;
    @Autowired
    private XBProjector xbProjector;

    @ShellMethod
    public void liveReloadInit(@ShellOption(defaultValue = ".") String baseDir) throws IOException, InterruptedException {
        ComponentFlow flow = flowBuilder.clone().reset()
                                        .withSingleItemSelector("css-framework")
                                        .name("CSS Framework to use")
                                        .selectItems(Map.of("Bootstrap", CSS_FRAMEWORK_BOOTSTRAP,
                                                            "Tailwind CSS", CSS_FRAMEWORK_TAILWIND_CSS))
                                        .and()
                                        .build();
        ComponentFlow.ComponentFlowResult flowResult = flow.run();
        String cssFrameworkSelection = flowResult.getContext().get("css-framework");
        LiveReloadInitStrategy strategy;
        if (cssFrameworkSelection.equals(CSS_FRAMEWORK_BOOTSTRAP)) {
            System.out.println("\uD83D\uDC85 Going with Bootstrap");
            strategy = new BootstrapLiveReloadInitStrategy(xbProjector);
        } else if (cssFrameworkSelection.equals(CSS_FRAMEWORK_TAILWIND_CSS)) {
            System.out.println("\uD83D\uDC85 Going with Tailwind CSS");
            strategy = new TailwindCssLiveReloadInitStrategy(xbProjector);
        } else {
            throw new IllegalArgumentException("unknown css framework: " + cssFrameworkSelection);
        }

        strategy.execute(new LiveReloadInitParameters(Path.of(baseDir)));
    }
}
