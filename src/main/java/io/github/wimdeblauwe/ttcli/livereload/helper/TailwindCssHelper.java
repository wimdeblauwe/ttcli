package io.github.wimdeblauwe.ttcli.livereload.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TailwindCssHelper {

    public static void createApplicationCss(Path base,
                                            String cssFilePath,
                                            String sourcePath) throws IOException {
        Path path = base.resolve(cssFilePath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, applicationCssContent(sourcePath));
    }

    private static String applicationCssContent(String sourcePath) {
        return """
                @import "tailwindcss";
                @source "%s";
                """.formatted(sourcePath);
    }

    private TailwindCssHelper() {
    }
}
