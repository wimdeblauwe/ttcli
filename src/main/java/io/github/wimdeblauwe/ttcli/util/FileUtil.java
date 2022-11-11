package io.github.wimdeblauwe.ttcli.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class FileUtil {
    public static boolean isEmpty(Path path) throws IOException {
        if (!Files.exists(path)) {
            return true;
        }

        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return entries.findFirst().isEmpty();
            }
        }

        return false;
    }
}
