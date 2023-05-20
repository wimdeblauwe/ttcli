package io.github.wimdeblauwe.ttcli.livereload;

public class LiveReloadInitServiceException extends RuntimeException {
    public LiveReloadInitServiceException(Throwable cause) {
        super(cause);
    }

    public LiveReloadInitServiceException(String message) {
        super(message);
    }
}
