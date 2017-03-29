package org.gradle.snapshot;

import org.gradle.snapshot.configuration.FileSnapshotOperation;
import org.gradle.snapshot.configuration.FileTreeOperation;
import org.gradle.snapshot.configuration.SnapshotterConfiguration;
import org.gradle.snapshot.configuration.SnapshotterModifier;
import org.gradle.snapshot.hashing.FileHasher;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

public class Snapshotter {
    private final FileHasher hasher;

    public Snapshotter(FileHasher hasher, SnapshotterConfiguration configuration) {
        this.hasher = hasher;
        this.configuration = configuration;
    }

    private SnapshotterConfiguration configuration;

    public Stream<FileSnapshot> snapshotFiles(Stream<File> fileTree) {
        return snapshot(fileTree.map(PhysicalFile::new));
    }

    public Stream<FileSnapshot> snapshot(Stream<SnapshottableFile> fileTree) {
        return fileTree
                .map(file -> {
                    Optional<FileSnapshot> operationApplied = configuration.getFileTreeOperation().flatMap(
                                    fileTreeOperation -> {
                                        if (!fileTreeOperation.getPredicate().test(file)) {
                                            return Optional.empty();
                                        }
                                        FileTreeOperation operation = fileTreeOperation.getOperation();
                                        return Optional.of(snapshot(operation.expand(file))
                                                .collect(operation.collector(file)));
                                    }
                            );
                            return operationApplied.map(Optional::of).orElseGet(() -> snapshotFile(file));
                        }
                ).flatMap(Snapshotter::streamOfOptional);
    }

    public static <T> Stream<T> streamOfOptional(Optional<T> optional) {
        return optional.map(Stream::of).orElseGet(Stream::empty);
    }

    private Optional<FileSnapshot> snapshotFile(SnapshottableFile file) {
        FileSnapshotOperation fileSnapshotOperation = configuration.getFileSnapshotOperation()
                .filter(modifier -> modifier.getPredicate().test(file))
                .map(SnapshotterModifier::getOperation)
                .orElse(FileSnapshotOperation.IDENTITY);
        return fileSnapshotOperation.transform(file).map(transformed -> new FileSnapshot(file.getPath(), hasher.hash(file)));
    }


}
