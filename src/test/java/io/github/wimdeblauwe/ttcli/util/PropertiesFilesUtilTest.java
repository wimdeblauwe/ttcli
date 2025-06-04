package io.github.wimdeblauwe.ttcli.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesFilesUtilTest {

    @TempDir
    Path tempDir;

    @Test
    void testWriteOrUpdatePropertiesFile_createNewFile() throws IOException {
        // Setup
        String fileName = "application.properties";
        String content = "property=value";

        // Execute
        PropertiesFilesUtil.writeOrUpdatePropertiesFile(tempDir, fileName, content);

        // Verify
        Path propertiesFile = tempDir.resolve("src/main/resources/" + fileName);
        assertThat(Files.exists(propertiesFile)).isTrue();
        assertThat(Files.readString(propertiesFile)).isEqualToNormalizingNewlines(content);
    }

    @Test
    void testWriteOrUpdatePropertiesFile_appendToExistingFile() throws IOException {
        // Setup
        String fileName = "application.properties";
        String initialContent = "property1=value1\n";
        String additionalContent = "property2=value2";

        // Create the initial file
        Path resourcesDir = tempDir.resolve("src/main/resources");
        Files.createDirectories(resourcesDir);
        Path propertiesFile = resourcesDir.resolve(fileName);
        Files.writeString(propertiesFile, initialContent, StandardOpenOption.CREATE);

        // Execute
        PropertiesFilesUtil.writeOrUpdatePropertiesFile(tempDir, fileName, additionalContent);

        // Verify
        assertThat(Files.readString(propertiesFile)).isEqualToNormalizingNewlines(initialContent + additionalContent);
    }

    @Test
    void testRemovePropertyFromPropertiesFile() throws IOException {
        // Setup
        Path resourcesDir = tempDir.resolve("src/main/resources");
        Files.createDirectories(resourcesDir);
        Path propertiesFile = resourcesDir.resolve("application.properties");

        // Create a properties file with some content
        String initialContent = """
                property1=value1
                gg.jte.development-mode=true
                property2=value2
                """;
        Files.writeString(propertiesFile, initialContent, StandardOpenOption.CREATE);

        // Execute
        PropertiesFilesUtil.removePropertyFromPropertiesFile(tempDir, "application.properties", "gg.jte.development-mode");

        // Verify that the property was removed
        String updatedContent = Files.readString(propertiesFile);
        assertThat(updatedContent).contains("property1=value1");
        assertThat(updatedContent).contains("property2=value2");
        assertThat(updatedContent).doesNotContain("gg.jte.development-mode=true");
    }

    @Test
    void testRemovePropertyFromPropertiesFile_propertyNotFound() throws IOException {
        // Setup
        Path resourcesDir = tempDir.resolve("src/main/resources");
        Files.createDirectories(resourcesDir);
        Path propertiesFile = resourcesDir.resolve("application.properties");

        // Create a properties file with some content
        String initialContent = """
                property1=value1
                property2=value2
                """;
        Files.writeString(propertiesFile, initialContent, StandardOpenOption.CREATE);

        // Execute
        PropertiesFilesUtil.removePropertyFromPropertiesFile(tempDir, "application.properties", "non.existent.property");

        // Verify that the content remains unchanged
        String updatedContent = Files.readString(propertiesFile);
        assertThat(updatedContent).isEqualToIgnoringNewLines(initialContent);
    }

    @Test
    void testRemovePropertyFromPropertiesFile_fileNotFound() throws IOException {
        // Execute - this should not throw an exception
        PropertiesFilesUtil.removePropertyFromPropertiesFile(tempDir, "non-existent.properties", "some.property");

        // Verify that the file was not created
        Path propertiesFile = tempDir.resolve("src/main/resources/non-existent.properties");
        assertThat(Files.exists(propertiesFile)).isFalse();
    }
}