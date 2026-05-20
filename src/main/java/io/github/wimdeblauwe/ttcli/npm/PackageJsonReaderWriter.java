package io.github.wimdeblauwe.ttcli.npm;

import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class PackageJsonReaderWriter {
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();
    private final Path path;
    private final PackageJsonModel packageJsonModel;

    public PackageJsonReaderWriter(Path path) throws IOException {
        this.path = path;
        packageJsonModel = JSON_MAPPER.readValue(path.toFile(), PackageJsonModel.class);
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

    public void setPnpmOnlyBuiltDependencies(List<String> onlyBuiltDependencies) {
        PackageJsonModel.PnpmConfig pnpm = packageJsonModel.getPnpm();
        if (pnpm == null) {
            pnpm = new PackageJsonModel.PnpmConfig();
            packageJsonModel.setPnpm(pnpm);
        }
        pnpm.setOnlyBuiltDependencies(onlyBuiltDependencies);
    }

    public void write() throws IOException {
        JSON_MAPPER.writeValue(path.toFile(), packageJsonModel);
    }
}
