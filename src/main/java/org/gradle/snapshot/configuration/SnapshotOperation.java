package org.gradle.snapshot.configuration;

import ix.Ix;
import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;
import org.gradle.snapshot.Snapshotter;

@FunctionalInterface
public interface SnapshotOperation {
    Ix<FileSnapshot> snapshot(SnapshottableFile file, SnapshotterContext context, Snapshotter snapshotter);
}
