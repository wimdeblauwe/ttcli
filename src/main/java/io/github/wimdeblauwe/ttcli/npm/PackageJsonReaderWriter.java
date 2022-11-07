package io.github.wimdeblauwe.ttcli.npm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class PackageJsonReaderWriter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final Path path;
    private final PackageJsonModel packageJsonModel;

    public PackageJsonReaderWriter(Path path) throws IOException {
        this.path = path;
        packageJsonModel = OBJECT_MAPPER.readValue(path.toFile(), PackageJsonModel.class);
    }

    public static PackageJsonReaderWriter readFrom(Path path) throws IOException {
        return new PackageJsonReaderWriter(path);
    }

    public void addScript(String name,
                          String contents) {
        packageJsonModel.addScript(name, contents);
    }

    public void addScripts(Map<String, String> scripts) {
        packageJsonModel.addScripts(scripts);
    }

    public void write() throws IOException {
        OBJECT_MAPPER.writeValue(path.toFile(), packageJsonModel);
    }
}
