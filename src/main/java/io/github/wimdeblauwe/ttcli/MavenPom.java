package io.github.wimdeblauwe.ttcli;

import org.xmlbeam.annotation.XBRead;

public interface MavenPom {
    @XBRead("/project/artifactId")
    String getArtifactId();
}
