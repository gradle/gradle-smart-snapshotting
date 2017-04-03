package org.gradle.snapshot.configuration;

import ix.Ix;
import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;
import org.gradle.snapshot.Snapshotter;

@FunctionalInterface
public interface SingleFileSnapshotOperation extends SnapshotOperation {

    @Override
    default Ix<FileSnapshot> snapshot(SnapshottableFile file, SnapshotterContext context, Snapshotter snapshotter) {
        return Ix.fromArray(snapshotSingleFile(file, context, snapshotter));
    }

    FileSnapshot snapshotSingleFile(SnapshottableFile file, SnapshotterContext context, Snapshotter snapshotter);
}
