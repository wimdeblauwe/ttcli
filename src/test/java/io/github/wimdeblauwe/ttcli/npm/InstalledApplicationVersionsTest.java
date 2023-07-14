package io.github.wimdeblauwe.ttcli.npm;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class InstalledApplicationVersionsTest {

    @Test
    void testNodeVersionBelowCurrentLtsVersion_versionOk() {
        InstalledApplicationVersions versions = new InstalledApplicationVersions("v18.16.1", "9.5.1");
        assertThat(versions.nodeVersionBelowCurrentLtsVersion()).isFalse();
    }

    @Test
    void testNodeVersionBelowCurrentLtsVersion_versionNotOk() {
        InstalledApplicationVersions versions = new InstalledApplicationVersions("v12.13.0", "6.12.0");
        assertThat(versions.nodeVersionBelowCurrentLtsVersion()).isTrue();
    }
}
