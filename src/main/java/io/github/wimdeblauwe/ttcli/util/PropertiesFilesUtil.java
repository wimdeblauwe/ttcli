package io.github.wimdeblauwe.ttcli.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Utility class for manipulating properties files.
 */
public final class PropertiesFilesUtil {

    /**
     * Private constructor to prevent instantiation.
     */
    private PropertiesFilesUtil() {
    }

    /**
     * Writes or updates a properties file with the given content.
     * If the file doesn't exist, it will be created.
     * If the file exists, the content will be appended to it.
     *
     * @param base     The base directory
     * @param fileName The name of the properties file
     * @param content  The content to write or append
     * @throws IOException If an I/O error occurs
     */
    public static void writeOrUpdatePropertiesFile(Path base,
                                                   String fileName,
                                                   String content) throws IOException {
        Path propertiesFile = base.resolve("src/main/resources/" +
                fileName);
        Files.createDirectories(propertiesFile.getParent());
        if (!Files.exists(propertiesFile)) {
            Files.writeString(propertiesFile, content, StandardOpenOption.CREATE);
        } else {
            Files.writeString(propertiesFile, content, StandardOpenOption.APPEND);
        }
    }

    /**
     * Removes a property from a properties file.
     * If the file doesn't exist, nothing happens.
     * If the property doesn't exist in the file, the file remains unchanged.
     *
     * @param base         The base directory
     * @param fileName     The name of the properties file
     * @param propertyName The name of the property to remove
     * @throws IOException If an I/O error occurs
     */
    public static void removePropertyFromPropertiesFile(Path base,
                                                        String fileName,
                                                        String propertyName) throws IOException {
        Path propertiesFile = base.resolve("src/main/resources/" + fileName);
        if (Files.exists(propertiesFile)) {
            String content = Files.readString(propertiesFile);
            String propertyLine = propertyName + "=";

            // Split the content into lines
            String[] lines = content.split("\\R");
            StringBuilder newContent = new StringBuilder();

            // Rebuild the content without the specified property
            for (String line : lines) {
                if (!line.trim().startsWith(propertyLine)) {
                    newContent.append(line).append(System.lineSeparator());
                }
            }

            // Write the updated content back to the file
            Files.writeString(propertiesFile, newContent.toString(), StandardOpenOption.TRUNCATE_EXISTING);
        }
    }
}