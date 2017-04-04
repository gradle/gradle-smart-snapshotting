package org.gradle.snapshot.operation.snapshot;

import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;
import org.gradle.snapshot.Snapshotter;
import org.gradle.snapshot.configuration.SnapshotterContext;

import java.util.stream.Stream;

@FunctionalInterface
public interface SnapshotOperation {
    Stream<FileSnapshot> snapshot(SnapshottableFile file, SnapshotterContext context, Snapshotter snapshotter);
}
