package org.gradle.snapshot.configuration;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

public class SnapshotterContext {
    private final SnapshotterModifier<FileSnapshotOperation> fileSnapshotOperation;
    private final List<SnapshotterModifier<FileTreeOperation>> fileTreeOperations;
    private final List<ContextElement> contextElements;

    public SnapshotterContext() {
        this(ImmutableList.of(), null, ImmutableList.of());
    }

    public SnapshotterContext(List<SnapshotterModifier<FileTreeOperation>> fileTreeOperations, SnapshotterModifier<FileSnapshotOperation> fileSnapshotOperation, List<ContextElement> contextElements) {
        this.fileSnapshotOperation = fileSnapshotOperation;
        this.fileTreeOperations = fileTreeOperations;
        this.contextElements = contextElements;
    }

    public List<SnapshotterModifier<FileTreeOperation>> getFileTreeOperations() {
        return fileTreeOperations;
    }

    public Optional<SnapshotterModifier<FileSnapshotOperation>> getFileSnapshotOperation() {
        return Optional.ofNullable(fileSnapshotOperation);
    }

    public SnapshotterContext withFileTreeOperations(Iterable<SnapshotterModifier<FileTreeOperation>> operations) {
        return new SnapshotterContext(ImmutableList.copyOf(operations), fileSnapshotOperation, contextElements);
    }

    public SnapshotterContext withFileTreeOperation(SnapshotterModifier<FileTreeOperation> operation) {
        return withFileTreeOperations(ImmutableList.<SnapshotterModifier<FileTreeOperation>>builder().addAll(fileTreeOperations).add(operation).build());
    }

    public SnapshotterContext withFileSnapshotterOperation(SnapshotterModifier<FileSnapshotOperation> operation) {
        return new SnapshotterContext(fileTreeOperations, operation, contextElements);
    }

    public SnapshotterContext addContext(ContextElement element) {
        return new SnapshotterContext(fileTreeOperations, fileSnapshotOperation,
                ImmutableList.<ContextElement>builder().addAll(contextElements).add(element).build());
    }

    public List<ContextElement> getContextElements() {
        return contextElements;
    }
}
