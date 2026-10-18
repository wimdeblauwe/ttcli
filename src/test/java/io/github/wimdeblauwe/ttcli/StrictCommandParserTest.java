package io.github.wimdeblauwe.ttcli;

import org.junit.jupiter.api.Test;
import org.springframework.shell.core.command.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StrictCommandParserTest {

    private final List<String> errors = new ArrayList<>();
    private final CommandRegistry registry = new CommandRegistry();
    private final StrictCommandParser parser;

    StrictCommandParserTest() {
        Command init = new AbstractCommand("init", "test") {
            @Override
            public ExitStatus doExecute(CommandContext commandContext) {
                return ExitStatus.OK;
            }
        };
        ((AbstractCommand) init).setOptions(List.of(
                CommandOption.with().longName("live-reload").type(String.class).build(),
                CommandOption.with().longName("non-interactive").type(boolean.class).build()));
        registry.registerCommand(init);
        parser = new StrictCommandParser(new DefaultCommandParser(registry), registry, errors::add);
    }

    @Test
    void acceptsKnownOptions() {
        ParsedInput parsed = parser.parse("init --live-reload vite --non-interactive");

        assertThat(parsed.options()).hasSize(2);
        assertThat(errors).isEmpty();
    }

    @Test
    void rejectsUnknownOption() {
        assertThatThrownBy(() -> parser.parse("init --web-dep htmx"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(errors).singleElement().asString()
                .contains("Unknown option '--web-dep'")
                .contains("--live-reload, --non-interactive");
    }

    @Test
    void reportsOptionWithoutValue() {
        assertThatThrownBy(() -> parser.parse("init --live-reload"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(errors).singleElement().asString().contains("'--live-reload' requires a value");
    }

    @Test
    void rejectsStrayArgumentFromUnquotedValueWithSpaces() {
        assertThatThrownBy(() -> parser.parse("init --live-reload vite App"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(errors).singleElement().asString().contains("Unexpected argument 'App'").contains("quoted twice");
    }

    @Test
    void acceptsQuotedValueWithSpaces() {
        ParsedInput parsed = parser.parse("init --live-reload \"my vite\"");

        assertThat(parsed.options().getFirst().value()).isEqualTo("my vite");
    }
}
