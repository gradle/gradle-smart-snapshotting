package org.gradle.snapshot.exceptions;

public class UncheckedIOException extends RuntimeException {
    public UncheckedIOException(String message, Throwable cause) {
        super(message, cause);
    }
}
