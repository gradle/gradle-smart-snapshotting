package org.gradle.snapshot.configuration;

import ix.Ix;
import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;
import org.gradle.snapshot.Snapshotter;

public interface FileTreeOperation extends SnapshotOperation {
    @Override
    default Ix<FileSnapshot> snapshot(SnapshottableFile file, SnapshotterContext context, Snapshotter snapshotter) {
        Ix<FileSnapshot> expandedSnapshots = snapshotter.snapshot(
                expand(file),
                modifyContext(context)
        );
        return collect(expandedSnapshots, file);
    }

    Ix<SnapshottableFile> expand(SnapshottableFile file);

    default SnapshotterContext modifyContext(SnapshotterContext context) {
        return context.addContextElement(new ContextElement(this.getClass()));
    }

    Ix<FileSnapshot> collect(Ix<FileSnapshot> snapshots, SnapshottableFile file);
}
