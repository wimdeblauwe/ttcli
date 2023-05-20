package io.github.wimdeblauwe.ttcli.util;

import io.github.wimdeblauwe.ttcli.ProjectInitializationServiceException;

import java.io.IOException;
import java.net.InetAddress;

public final class InetUtil {
    public static void checkIfInternetAccessIsAvailable(String host) {
        try {
            InetAddress address = InetAddress.getByName(host);
            if (!address.isReachable(5000)) {
                String message = String.format("Unable to connect to %s. Is your internet connection enabled?", host);
                throw new ProjectInitializationServiceException(message);
            }
        } catch (IOException e) {
            String message = String.format("Unable to connect to %s. Is your internet connection enabled?\nError: %s", host, e.getMessage());
            throw new ProjectInitializationServiceException(message);
        }
    }

    private InetUtil() {
    }
}
