package io.github.wimdeblauwe.ttcli;

import org.xmlbeam.annotation.XBRead;

public interface MavenPom {
    @XBRead("/project/version")
    String getVersion();

    @XBRead("/project/artifactId")
    String getArtifactId();


}
