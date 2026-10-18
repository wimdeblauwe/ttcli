package io.github.wimdeblauwe.ttcli.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ZipUtilTest {

    @Test
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows has no executable file permission")
    void unzipMakesWrapperScriptsExecutable(@TempDir Path tempDir) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (String name : new String[]{"mvnw", "sub/gradlew", "pom.xml"}) {
                zip.putNextEntry(new ZipEntry(name));
                zip.write("content".getBytes());
                zip.closeEntry();
            }
        }

        ZipUtil.unzip(bytes.toByteArray(), tempDir);

        assertThat(Files.isExecutable(tempDir.resolve("mvnw"))).isTrue();
        assertThat(Files.isExecutable(tempDir.resolve("sub/gradlew"))).isTrue();
        assertThat(Files.isExecutable(tempDir.resolve("pom.xml"))).isFalse();
    }
}
