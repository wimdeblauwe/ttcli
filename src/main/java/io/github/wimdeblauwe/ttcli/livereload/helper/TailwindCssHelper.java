package io.github.wimdeblauwe.ttcli.livereload.helper;

import io.github.wimdeblauwe.ttcli.util.ProcessBuilderFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class TailwindCssHelper {

    public static void createApplicationCss(Path base,
                                            String cssFilePath) throws IOException {
        Path path = base.resolve(cssFilePath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, applicationCssContent());
    }

    public static void setupTailwindConfig(Path base,
                                           String contentPath) throws IOException, InterruptedException {
        initializeTailwindConfig(base);

        // Point tailwind to Thymeleaf templates
        Path tailwindConfigFilePath = base.resolve("tailwind.config.js");
        byte[] bytes = Files.readAllBytes(tailwindConfigFilePath);
        String s = new String(bytes);
        s = s.replaceFirst("content: \\[]", "content: ['" + contentPath + "']");
        Files.writeString(tailwindConfigFilePath, s);
    }

    private static void initializeTailwindConfig(Path base) throws InterruptedException, IOException {
        ProcessBuilder builder = ProcessBuilderFactory.create(List.of("npx", "tailwindcss", "init"));
        builder.directory(base.toFile());
        int exitValue = builder.start().waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("unable to init tailwind css");
        }
    }

    private static String applicationCssContent() {
        return """
                @tailwind base;
                @tailwind components;
                @tailwind utilities;""";
    }

    private TailwindCssHelper() {
    }
}
