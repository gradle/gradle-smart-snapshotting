package org.gradle.snapshot;

import io.reactivex.Observable;
import org.gradle.snapshot.configuration.SnapshotterContext;

public interface Snapshotter {
    Observable<FileSnapshot> snapshot(Observable<SnapshottableFile> fileTree, SnapshotterContext context);
}
