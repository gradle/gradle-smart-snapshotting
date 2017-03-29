package org.gradle.snapshot;

import org.gradle.snapshot.configuration.FileTreeOperation;
import org.gradle.snapshot.configuration.SnapshotterConfiguration;
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
                .map(file -> configuration.getFileTreeOperation().flatMap(
                        fileTreeOperation -> {
                            if (!fileTreeOperation.getPredicate().test(file)) {
                                return Optional.empty();
                            }
                            FileTreeOperation operation = fileTreeOperation.getOperation();
                            return Optional.of(snapshot(operation.expand(file))
                                    .collect(operation.collector(file)));
                        }
                ).orElseGet(() -> snapshotFile(file)));
    }

    private FileSnapshot snapshotFile(SnapshottableFile file) {
        return new FileSnapshot(file.getPath(), hasher.hash(file));
    }


}
