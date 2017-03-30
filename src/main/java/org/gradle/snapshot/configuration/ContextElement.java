package org.gradle.snapshot.configuration;

public class ContextElement {
    private final Class<? extends FileTreeOperation> operation;

    public ContextElement(Class<? extends FileTreeOperation> operation) {
        this.operation = operation;
    }

    public Class<? extends FileTreeOperation> getOperation() {
        return operation;
    }
}
