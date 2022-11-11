package io.github.wimdeblauwe.ttcli.util;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ZipUtil {
    public static void unzip(byte[] zipContents,
                      Path outputPath) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipContents))) {
            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                Path entryOutputPath = outputPath.resolve(zipEntry.getName());

                if (zipEntry.isDirectory()) {
                    Files.createDirectories(entryOutputPath);
                } else {
                    Files.createDirectories(entryOutputPath.getParent());
                    writeContents(zipInputStream, entryOutputPath);
                }
                zipInputStream.closeEntry();
            }
        }
    }

    private static void writeContents(ZipInputStream zipInputStream,
                                      Path outputPath)
            throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputPath.toFile())) {
            int len;
            byte[] content = new byte[2048];
            while ((len = zipInputStream.read(content)) > 0) {
                fileOutputStream.write(content, 0, len);
            }
        }
    }
}
