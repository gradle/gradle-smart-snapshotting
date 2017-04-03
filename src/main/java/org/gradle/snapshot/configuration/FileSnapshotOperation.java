package org.gradle.snapshot.configuration;

import io.reactivex.Observable;
import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;
import org.gradle.snapshot.Snapshotter;



@FunctionalInterface
public interface FileSnapshotOperation extends SnapshotOperation {
    @Override
    default Observable<FileSnapshot> snapshot(SnapshottableFile file, SnapshotterContext context, Snapshotter snapshotter) {
        return snapshotter.snapshot(Observable.fromArray(transform(file)), context.withBindings(context.getBindings().withoutSnapshotOperation(this)));
    }

    SnapshottableFile transform(SnapshottableFile snapshottableFile);
}
