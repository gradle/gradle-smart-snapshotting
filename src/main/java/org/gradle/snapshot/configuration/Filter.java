package org.gradle.snapshot.configuration;

import io.reactivex.Observable;
import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;

public class Filter implements FileTreeOperation {
    @Override
    public Observable<SnapshottableFile> expand(SnapshottableFile file) {
        return Observable.empty();
    }

    @Override
    public Observable<FileSnapshot> collect(Observable<FileSnapshot> snapshots, SnapshottableFile file) {
        return snapshots;
    }
}
