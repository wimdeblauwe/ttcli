package io.github.wimdeblauwe.ttcli.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleOutputTest {

    @Test
    void supportsEmojiOnUtf8Terminal() {
        assertThat(ConsoleOutput.supportsEmoji("UTF-8", Map.of("TERM", "xterm-256color"))).isTrue();
        assertThat(ConsoleOutput.supportsEmoji("utf8", Map.of())).isTrue();
    }

    @Test
    void noEmojiOnNonUtf8Encoding() {
        assertThat(ConsoleOutput.supportsEmoji("Cp437", Map.of())).isFalse();
        assertThat(ConsoleOutput.supportsEmoji("windows-1252", Map.of())).isFalse();
    }

    @Test
    void noEmojiOnLimitedTerminals() {
        assertThat(ConsoleOutput.supportsEmoji("UTF-8", Map.of("TERM", "dumb"))).isFalse();
        assertThat(ConsoleOutput.supportsEmoji("UTF-8", Map.of("TERM", "linux"))).isFalse();
    }

    @Test
    void noEmojiWhenExplicitlyDisabled() {
        assertThat(ConsoleOutput.supportsEmoji("UTF-8", Map.of("NO_EMOJI", "1"))).isFalse();
    }
}
