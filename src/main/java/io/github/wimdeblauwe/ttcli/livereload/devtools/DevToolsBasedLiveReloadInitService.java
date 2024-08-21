package io.github.wimdeblauwe.ttcli.livereload.devtools;

import io.github.wimdeblauwe.ttcli.ProjectInitializationParameters;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitService;
import io.github.wimdeblauwe.ttcli.livereload.LiveReloadInitServiceException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Set;

@Component
@Order(3)
public class DevToolsBasedLiveReloadInitService implements LiveReloadInitService {
    @Override
    public String getId() {
        return "dev-tools-based";
    }

    @Override
    public String getName() {
        return "DevTools based";
    }

    @Override
    public String getHelpText() {
        return """
                # Live reload setup
                                
                This project uses Spring Boot DevTools to have live reloading.
                                
                Use the following steps to get it working:
                                
                1. Install the LiveReload browser extension in your browser.
                2. Configure your editor to automatically compile when saving. For IntelliJ, you need to enable 'Build project automatically' in the project settings.
                   Also enable 'Allow auto-make to start even if developed application is currently running'.
                3. Run the Spring Boot application.
                4. Open the browser at http://localhost:8080. Ensure the Live Reload extension is active in the browser.
                                
                You should now be able to change any HTML or CSS and have the browser reload upon saving the file.
                """;
    }

    @Override
    public Set<String> additionalSpringInitializrDependencies() {
        return Set.of("devtools");
    }

    @Override
    public void generate(ProjectInitializationParameters projectInitializationParameters) throws LiveReloadInitServiceException {
    }

    @Override
    public void runBuild(ProjectInitializationParameters projectInitializationParameters) throws LiveReloadInitServiceException {
    }

    @Override
    public Path getTailwindConfigFileParentPath(ProjectInitializationParameters parameters) {
        return null;
    }
}
