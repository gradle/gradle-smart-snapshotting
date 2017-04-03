package org.gradle.snapshot.configuration;

import io.reactivex.Observable;
import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;
import org.gradle.snapshot.Snapshotter;

@FunctionalInterface
public interface SingleFileSnapshotOperation extends SnapshotOperation {

    @Override
    default Observable<FileSnapshot> snapshot(SnapshottableFile file, SnapshotterContext context, Snapshotter snapshotter) {
        return Observable.fromArray(snapshotSingleFile(file, context, snapshotter));
    }

    FileSnapshot snapshotSingleFile(SnapshottableFile file, SnapshotterContext context, Snapshotter snapshotter);
}
