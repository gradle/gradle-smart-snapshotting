package org.gradle.snapshot.operation.snapshot;

import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;
import org.gradle.snapshot.Snapshotter;
import org.gradle.snapshot.configuration.ContextElement;
import org.gradle.snapshot.configuration.SnapshotterContext;

import java.util.stream.Stream;

public interface FileTreeOperation extends SnapshotOperation {
    @Override
    default Stream<FileSnapshot> snapshot(SnapshottableFile file, SnapshotterContext context, Snapshotter snapshotter) {
        Stream<FileSnapshot> expandedSnapshots = snapshotter.snapshot(
                expand(file),
                modifyContext(context)
        );
        return collect(expandedSnapshots, file);
    }

    Stream<SnapshottableFile> expand(SnapshottableFile file);

    default SnapshotterContext modifyContext(SnapshotterContext context) {
        return context.addContextElement(new ContextElement(this.getClass()));
    }

    Stream<FileSnapshot> collect(Stream<FileSnapshot> snapshots, SnapshottableFile file);
}
