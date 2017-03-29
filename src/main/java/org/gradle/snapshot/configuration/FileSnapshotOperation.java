package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;

import java.util.Optional;

public interface FileSnapshotOperation {
    FileSnapshotOperation IDENTITY = Optional::of;

    Optional<SnapshottableFile> transform(SnapshottableFile snapshottableFile);
}
