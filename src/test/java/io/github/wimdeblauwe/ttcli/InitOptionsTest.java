package io.github.wimdeblauwe.ttcli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InitOptionsTest {

    @Test
    void blankValuesAreTreatedAsNotGiven() {
        InitOptions options = new InitOptions("", "  ", null, " 4.1.1 ", null, null, null, "vite", null, "", null, null, null, true);

        assertThat(options.group()).isNull();
        assertThat(options.artifact()).isNull();
        assertThat(options.springBootVersion()).isEqualTo("4.1.1");
        assertThat(options.liveReload()).isEqualTo("vite");
        assertThat(options.webDeps()).isNull();
        assertThat(options.nonInteractive()).isTrue();
    }
}
