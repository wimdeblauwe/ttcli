package io.github.wimdeblauwe.ttcli.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ExternalProcessRunner {

    public static String run(File directory,
                             List<String> parameters,
                             Supplier<String> errorMessageSupplier) throws InterruptedException {
        List<String> command = new ArrayList<>();
        if (System.getProperty("os.name").contains("indows")) {
            // We need to add "cmd /c" on Windows so that applications on the path are found by ProcessBuilder
            command.addAll(List.of("cmd", "/c"));
        }
        command.addAll(parameters);
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(directory);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        builder.redirectErrorStream(true);

        try {
            Process process = builder.start();
            final InputStream stdoutInputStream = process.getInputStream();
            final BufferedReader stdoutReader =
                    new BufferedReader(new InputStreamReader(stdoutInputStream));
            StringBuilder output = new StringBuilder();
            stdoutReader.lines().forEach(str -> output.append(str).append(System.lineSeparator()));
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                throw new ExternalProcessException(errorMessageSupplier.get() + "\n" + output);
            }
            return output.toString();
        } catch (IOException e) {
            throw new ExternalProcessException(errorMessageSupplier.get(), e);
        }
    }

    private ExternalProcessRunner() {
    }
}
