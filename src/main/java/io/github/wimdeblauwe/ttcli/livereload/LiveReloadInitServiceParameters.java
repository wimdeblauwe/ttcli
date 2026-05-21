package io.github.wimdeblauwe.ttcli.livereload;

import io.github.wimdeblauwe.ttcli.npm.PackageManager;

public record LiveReloadInitServiceParameters(String initServiceId,
                                              PackageManager packageManager) {
}
