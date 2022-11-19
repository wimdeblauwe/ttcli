package io.github.wimdeblauwe.ttcli.help;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
public class HelpTextInitService {
    public void addHelpText(Path basePath,
                            String helpText) throws IOException {
        Path helpPath = basePath.resolve("HELP.md");
        Files.writeString(helpPath, helpText, StandardOpenOption.APPEND);
    }
}
