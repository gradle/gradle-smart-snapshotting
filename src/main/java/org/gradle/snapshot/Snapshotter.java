package org.gradle.snapshot;

import ix.Ix;
import org.gradle.snapshot.configuration.SnapshotterContext;

public interface Snapshotter {
    Ix<FileSnapshot> snapshot(Ix<SnapshottableFile> fileTree, SnapshotterContext context);
}
