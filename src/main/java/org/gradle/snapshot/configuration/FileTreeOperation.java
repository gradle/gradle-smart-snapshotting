package org.gradle.snapshot.configuration;

import io.reactivex.Observable;
import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;
import org.gradle.snapshot.Snapshotter;

public interface FileTreeOperation extends SnapshotOperation {
    @Override
    default Observable<FileSnapshot> snapshot(SnapshottableFile file, SnapshotterContext context, Snapshotter snapshotter) {
        Observable<FileSnapshot> expandedSnapshots = snapshotter.snapshot(
                expand(file),
                modifyContext(context)
        );
        return collect(expandedSnapshots, file);
    }

    Observable<SnapshottableFile> expand(SnapshottableFile file);

    default SnapshotterContext modifyContext(SnapshotterContext context) {
        return context.addContextElement(new ContextElement(this.getClass()));
    }

    Observable<FileSnapshot> collect(Observable<FileSnapshot> snapshots, SnapshottableFile file);
}
