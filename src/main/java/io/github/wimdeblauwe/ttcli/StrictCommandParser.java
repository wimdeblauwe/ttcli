package io.github.wimdeblauwe.ttcli;

import org.springframework.shell.core.command.Command;
import org.springframework.shell.core.command.CommandOption;
import org.springframework.shell.core.command.CommandParser;
import org.springframework.shell.core.command.CommandRegistry;
import org.springframework.shell.core.command.ParsedInput;

import java.util.List;
import java.util.function.Consumer;

/**
 * Spring Shell silently ignores unknown options and swallows parse errors (such as an option without a value),
 * which makes the process exit with code 0. For scripts and coding agents that would hide typos, so this parser
 * reports them through {@code onError}, which is expected to terminate the process with a non-zero code.
 */
public class StrictCommandParser implements CommandParser {
    private final CommandParser delegate;
    private final CommandRegistry commandRegistry;
    private final Consumer<String> onError;

    public StrictCommandParser(CommandParser delegate, CommandRegistry commandRegistry, Consumer<String> onError) {
        this.delegate = delegate;
        this.commandRegistry = commandRegistry;
        this.onError = onError;
    }

    @Override
    public ParsedInput parse(String input) {
        ParsedInput parsed;
        try {
            parsed = delegate.parse(input);
        } catch (IllegalArgumentException e) {
            onError.accept(e.getMessage());
            throw e;
        }
        Command command = commandRegistry.getCommandByName(parsed.commandName());
        if (command != null) {
            List<CommandOption> knownOptions = command.getOptions();
            for (CommandOption option : parsed.options()) {
                boolean known = option.longName() != null && !option.longName().isEmpty()
                        ? knownOptions.stream().anyMatch(k -> k.isOptionEqual("--" + option.longName()))
                        : knownOptions.stream().anyMatch(k -> k.isOptionEqual("-" + option.shortName()));
                if (!known && !isHelp(option)) {
                    String name = option.longName() != null && !option.longName().isEmpty() ? "--" + option.longName() : "-" + option.shortName();
                    String message = "Unknown option '%s' for command '%s'. Valid options: %s".formatted(name, parsed.commandName(),
                            String.join(", ", knownOptions.stream().map(k -> "--" + k.longName()).toList()));
                    onError.accept(message);
                    throw new IllegalArgumentException(message);
                }
            }
        }
        if (command != null && !command.getOptions().isEmpty() && !parsed.arguments().isEmpty()) {
            String message = "Unexpected argument '%s' for command '%s'. Values with spaces must be quoted twice, for example --name '\"My App\"'"
                    .formatted(parsed.arguments().getFirst().value(), parsed.commandName());
            onError.accept(message);
            throw new IllegalArgumentException(message);
        }
        return parsed;
    }

    private static boolean isHelp(CommandOption option) {
        return "help".equals(option.longName()) || option.shortName() == 'h';
    }
}
