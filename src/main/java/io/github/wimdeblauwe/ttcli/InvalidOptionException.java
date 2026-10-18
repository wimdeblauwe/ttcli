package io.github.wimdeblauwe.ttcli;

/**
 * Thrown when a command-line option of {@code init} has an invalid value or conflicts with another answer.
 */
public class InvalidOptionException extends ProjectInitializationServiceException {
    public InvalidOptionException(String message) {
        super(message);
    }
}
