package org.gradle.snapshot;

import org.gradle.snapshot.configuration.SnapshotOperation;
import org.gradle.snapshot.configuration.SnapshotOperationBindings;
import org.gradle.snapshot.configuration.SnapshotterContext;
import org.gradle.snapshot.hashing.FileHasher;

import java.io.File;
import java.util.stream.Stream;

public class DefaultSnapshotter implements Snapshotter {
    private final FileHasher hasher;

    public DefaultSnapshotter(FileHasher hasher) {
        this.hasher = hasher;
    }

    public Stream<FileSnapshot> snapshotFiles(Stream<? extends File> fileTree, SnapshotOperationBindings bindings) {
        return snapshot(fileTree.map(PhysicalFile::new), new SnapshotterContext().withBindings(bindings));
    }

    public Stream<FileSnapshot> snapshot(Stream<SnapshottableFile> fileTree, SnapshotterContext context) {
        return fileTree
                .flatMap(file -> context.getBindings().stream()
                        .filter(op -> op.getBoundTo().test(file, context.getContextElements()))
                        .findFirst().map(
                                fileTreeOperation -> {
                                    SnapshotOperation operation = fileTreeOperation.getOperation();
                                    return operation.snapshot(file,
                                            context,
                                            this);
                                }
                        ).orElseGet(() -> Stream.of(snapshotFile(file))));
    }

    private FileSnapshot snapshotFile(SnapshottableFile file) {
        return new FileSnapshot(file.getPath(), hasher.hash(file));
    }


}
