package org.gradle.snapshot.configuration;

import ix.Ix;
import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;
import org.gradle.snapshot.Snapshotter;


@FunctionalInterface
public interface FileSnapshotOperation extends SnapshotOperation {
    @Override
    default Ix<FileSnapshot> snapshot(SnapshottableFile file, SnapshotterContext context, Snapshotter snapshotter) {
        return snapshotter.snapshot(Ix.fromArray(transform(file)), context.withBindings(context.getBindings().withoutSnapshotOperation(this)));
    }

    SnapshottableFile transform(SnapshottableFile snapshottableFile);
}
