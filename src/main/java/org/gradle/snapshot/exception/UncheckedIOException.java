package org.gradle.snapshot.exception;

public class UncheckedIOException extends RuntimeException {
    public UncheckedIOException(String message, Throwable cause) {
        super(message, cause);
    }
}
