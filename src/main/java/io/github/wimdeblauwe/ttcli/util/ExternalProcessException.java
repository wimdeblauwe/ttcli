package io.github.wimdeblauwe.ttcli.util;

public class ExternalProcessException extends RuntimeException {
    public ExternalProcessException(String message) {
        super(message);
    }

    public ExternalProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
