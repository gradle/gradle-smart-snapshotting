package org.gradle.snapshot.configuration;

import java.util.Optional;

public class SnapshotterConfiguration {
    private SnapshotterModifier<FileSnapshotOperation> fileSnapshotOperation;
    private SnapshotterModifier<FileTreeOperation> fileTreeOperation;

    public SnapshotterConfiguration() {
    }

    public SnapshotterConfiguration(SnapshotterModifier<FileTreeOperation> fileTreeOperation, SnapshotterModifier<FileSnapshotOperation> fileSnapshotOperation) {
        this.fileSnapshotOperation = fileSnapshotOperation;
        this.fileTreeOperation = fileTreeOperation;
    }

    public SnapshotterConfiguration(SnapshotterModifier<FileTreeOperation> fileTreeOperation) {
        this(fileTreeOperation, null);
    }

    public Optional<SnapshotterModifier<FileTreeOperation>> getFileTreeOperation() {
        return Optional.ofNullable(fileTreeOperation);
    }

    public Optional<SnapshotterModifier<FileSnapshotOperation>> getFileSnapshotOperation() {
        return Optional.ofNullable(fileSnapshotOperation);
    }
}
