package io.github.wimdeblauwe.ttcli.npm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record InstalledApplicationVersions(String nodeVersion,
                                           String npmVersion) {
    private static final Pattern NODE_VERSION_PATTERN_MAJOR = Pattern.compile("v([0-9]+)\\.");
    private static final int NODE_CURRENT_LTS_MAJOR_VERSION = 18;

    public boolean nodeVersionBelowCurrentLtsVersion() {
        Matcher matcher = NODE_VERSION_PATTERN_MAJOR.matcher(nodeVersion);
        if (matcher.find()) {
            String group = matcher.group(1);
            return Integer.parseInt(group) < NODE_CURRENT_LTS_MAJOR_VERSION;
        } else {
            return false; //  Could not find the version number, no idea if we should show the warning.
        }
    }
}
