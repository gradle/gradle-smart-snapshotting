package org.gradle.snapshot.configuration;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

public class SnapshotterConfiguration {
    private SnapshotterModifier<FileSnapshotOperation> fileSnapshotOperation;
    private List<SnapshotterModifier<FileTreeOperation>> fileTreeOperations = ImmutableList.of();

    public SnapshotterConfiguration() {
    }

    public SnapshotterConfiguration(List<SnapshotterModifier<FileTreeOperation>> fileTreeOperations, SnapshotterModifier<FileSnapshotOperation> fileSnapshotOperation) {
        this.fileSnapshotOperation = fileSnapshotOperation;
        this.fileTreeOperations = fileTreeOperations;
    }

    @SafeVarargs
    public SnapshotterConfiguration(SnapshotterModifier<FileTreeOperation>... fileTreeOperations) {
        this(ImmutableList.copyOf(fileTreeOperations), null);
    }

    public List<SnapshotterModifier<FileTreeOperation>> getFileTreeOperations() {
        return fileTreeOperations;
    }

    public Optional<SnapshotterModifier<FileSnapshotOperation>> getFileSnapshotOperation() {
        return Optional.ofNullable(fileSnapshotOperation);
    }
}
