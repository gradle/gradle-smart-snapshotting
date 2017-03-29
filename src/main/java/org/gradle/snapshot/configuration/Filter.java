package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;

import java.util.Optional;

public class Filter implements FileSnapshotOperation {
    @Override
    public Optional<SnapshottableFile> transform(SnapshottableFile snapshottableFile) {
        return Optional.empty();
    }
}
