package org.gradle.snapshot.configuration;

public class ContextElement {
    private final Class<?> operation;

    public ContextElement(Class<?> operation) {
        this.operation = operation;
    }

    public Class<?> getOperation() {
        return operation;
    }
}
