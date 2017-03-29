package org.gradle.snapshot.configuration;

import java.util.Optional;

public class SnapshotterConfiguration {
    public SnapshotterConfiguration() {
    }

    public SnapshotterConfiguration(FileTreeOperation fileTreeOperation) {
        this.fileTreeOperation = fileTreeOperation;
    }

    private FileTreeOperation fileTreeOperation;

    public Optional<FileTreeOperation> getFileTreeOperation() {
        return Optional.ofNullable(fileTreeOperation);
    }
}
