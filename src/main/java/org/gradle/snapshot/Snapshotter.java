package org.gradle.snapshot;

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
                .flatMap(file -> configuration.getFileTreeOperation().flatMap(
                        fileTreeOperation -> {
                            if (!fileTreeOperation.getPredicate().test(file)) {
                                return Optional.empty();
                            }
                            FileTreeOperation operation = fileTreeOperation.getOperation();
                            return Optional.of(operation.collect(snapshot(operation.expand(file)), file));
                        }
                ).orElseGet(() -> Stream.of(snapshotFile(file))));
    }

    private FileSnapshot snapshotFile(SnapshottableFile file) {
        SnapshottableFile transformedFile = configuration.getFileSnapshotOperation()
                .filter(modifier -> modifier.getPredicate().test(file))
                .map(SnapshotterModifier::getOperation)
                .orElse(s -> s).transform(file);
        return new FileSnapshot(transformedFile.getPath(), hasher.hash(transformedFile));
    }


}
