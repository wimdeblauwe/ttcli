package io.github.wimdeblauwe.ttcli.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileUtilTest {

    @TempDir
    Path tempDir;

    @Test
    void testIsEmpty_nonExistentPath() throws IOException {
        Path nonExistentPath = tempDir.resolve("non-existent");
        assertThat(FileUtil.isEmpty(nonExistentPath)).isTrue();
    }

    @Test
    void testIsEmpty_emptyDirectory() throws IOException {
        Path emptyDir = tempDir.resolve("empty-dir");
        Files.createDirectory(emptyDir);
        assertThat(FileUtil.isEmpty(emptyDir)).isTrue();
    }

    @Test
    void testIsEmpty_nonEmptyDirectory() throws IOException {
        Path nonEmptyDir = tempDir.resolve("non-empty-dir");
        Files.createDirectory(nonEmptyDir);
        Files.createFile(nonEmptyDir.resolve("file.txt"));
        assertThat(FileUtil.isEmpty(nonEmptyDir)).isFalse();
    }

    @Test
    void testIsEmpty_file() throws IOException {
        Path file = tempDir.resolve("file.txt");
        Files.createFile(file);
        assertThat(FileUtil.isEmpty(file)).isFalse();
    }
}