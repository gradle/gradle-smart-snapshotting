package org.gradle.snapshot;

import org.gradle.snapshot.configuration.SnapshotterContext;

import java.util.stream.Stream;

public interface Snapshotter {
    Stream<FileSnapshot> snapshot(Stream<SnapshottableFile> fileTree, SnapshotterContext context);
}
