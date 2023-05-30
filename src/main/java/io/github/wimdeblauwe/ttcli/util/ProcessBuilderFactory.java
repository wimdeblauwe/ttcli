package io.github.wimdeblauwe.ttcli.util;

import java.util.ArrayList;
import java.util.List;

public final class ProcessBuilderFactory {
    public static ProcessBuilder create(List<String> parameters) {
        List<String> command = new ArrayList<>();
        if (System.getProperty("os.name").contains("indows")) {
            // We need to add "cmd /c" on Windows so that applications on the path are found by ProcessBuilder
            command.addAll(List.of("cmd", "/c"));
        }
        command.addAll(parameters);
        return new ProcessBuilder(command);
    }

    private ProcessBuilderFactory() {
    }
}
