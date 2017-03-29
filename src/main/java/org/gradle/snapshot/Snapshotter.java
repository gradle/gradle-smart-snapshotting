package org.gradle.snapshot;

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

    public Stream<FileSnapshot> snapshot(Stream<File> fileTree) {
        return fileTree
                .map(file -> configuration.getFileTreeOperation().flatMap(
                        fileTreeOperation -> {
                            if (!fileTreeOperation.applies(file)) {
                                return Optional.empty();
                            }
                            return Optional.of(fileTreeOperation.expand(file)
                                    .map(this::snapshotFile)
                                    .collect(fileTreeOperation.collector(file)));
                        }
                ).orElseGet(() -> snapshotFile(new PhysicalFile(file))));
    }

    private FileSnapshot snapshotFile(SnapshottableFile file) {
        return new FileSnapshot(file.getPath(), hasher.hash(file));
    }


}
