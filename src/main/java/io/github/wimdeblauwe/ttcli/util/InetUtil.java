package io.github.wimdeblauwe.ttcli.util;

import io.github.wimdeblauwe.ttcli.ProjectInitializationServiceException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class InetUtil {
    public static void checkIfInternetAccessIsAvailable(String host) {
        try {
            boolean reachable = isReachable(host, 443, 5000);
            if (!reachable) {
                String message = String.format("Unable to connect to %s. Is your internet connection enabled?", host);
                throw new ProjectInitializationServiceException(message);
            }
        } catch (IOException e) {
            String message = String.format("Unable to connect to %s. Is your internet connection enabled?\nError: %s", host, e.getMessage());
            throw new ProjectInitializationServiceException(message);
        }
    }

    private static boolean isReachable(String addr,
                                       int openPort,
                                       int timeOutMillis) throws IOException {
        // Any Open port on other machine
        // openPort =  22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.
        try (Socket soc = new Socket()) {
            soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
            return true;
        }
    }

    private InetUtil() {
    }
}
