package org.gradle.snapshot;

import io.reactivex.Observable;
import org.gradle.snapshot.configuration.SnapshotOperation;
import org.gradle.snapshot.configuration.SnapshotOperationBindings;
import org.gradle.snapshot.configuration.SnapshotterContext;
import org.gradle.snapshot.hashing.FileHasher;

import java.io.File;
import java.util.Optional;

public class DefaultSnapshotter implements Snapshotter {
    private final FileHasher hasher;

    public DefaultSnapshotter(FileHasher hasher) {
        this.hasher = hasher;
    }

    public Observable<FileSnapshot> snapshotFiles(Observable<? extends File> fileTree, SnapshotOperationBindings bindings) {
        return snapshot(fileTree.map(PhysicalFile::new), new SnapshotterContext().withBindings(bindings));
    }

    public Observable<FileSnapshot> snapshot(Observable<SnapshottableFile> fileTree, SnapshotterContext context) {
        return fileTree
                .flatMap(file -> Observable.fromIterable(context.getBindings())
                        .filter(op -> op.getBoundTo().test(file, context.getContextElements()))
                        .map(Optional::of).firstElement().blockingGet(Optional.empty()).map(
                                fileTreeOperation -> {
                                    SnapshotOperation operation = fileTreeOperation.getOperation();
                                    return operation.snapshot(file,
                                            context,
                                            this);
                                }
                        ).orElseGet(() -> Observable.fromArray(snapshotFile(file))));
    }

    private FileSnapshot snapshotFile(SnapshottableFile file) {
        return new FileSnapshot(file.getPath(), hasher.hash(file));
    }


}
