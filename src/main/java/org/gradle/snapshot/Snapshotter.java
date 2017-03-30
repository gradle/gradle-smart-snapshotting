package org.gradle.snapshot;

import org.gradle.snapshot.configuration.ContextElement;
import org.gradle.snapshot.configuration.FileTreeOperation;
import org.gradle.snapshot.configuration.SnapshotterContext;
import org.gradle.snapshot.configuration.SnapshotterModifier;
import org.gradle.snapshot.hashing.FileHasher;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

public class Snapshotter {
    private final FileHasher hasher;

    public Snapshotter(FileHasher hasher) {
        this.hasher = hasher;
    }

    public Stream<FileSnapshot> snapshotFiles(Stream<? extends File> fileTree, SnapshotterContext context) {
        return snapshot(fileTree.map(PhysicalFile::new), context);
    }

    public Stream<FileSnapshot> snapshot(Stream<SnapshottableFile> fileTree, SnapshotterContext context) {
        return fileTree
                .flatMap(file -> context.getFileTreeOperations().stream()
                        .filter(op -> op.getFilePredicate().test(file) && op.getContextPredicate().test(context.getContextElements()))
                        .findFirst().flatMap(
                        fileTreeOperation -> {
                            FileTreeOperation operation = fileTreeOperation.getOperation();
                            return Optional.of(operation.collect(snapshot(operation.expand(file),
                                    context.addContext(new ContextElement(operation.getClass()))),
                                    file));
                        }
                ).orElseGet(() -> Stream.of(snapshotFile(file, context))));
    }

    private FileSnapshot snapshotFile(SnapshottableFile file, SnapshotterContext context) {
        SnapshottableFile transformedFile = context.getFileSnapshotOperation()
                .filter(modifier -> modifier.getFilePredicate().test(file))
                .map(SnapshotterModifier::getOperation)
                .orElse(s -> s).transform(file);
        return new FileSnapshot(transformedFile.getPath(), hasher.hash(transformedFile));
    }


}
