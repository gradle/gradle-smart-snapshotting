package org.gradle.snapshot;

import org.gradle.snapshot.hashing.FileHasher;

import java.io.File;
import java.util.stream.Stream;

public class Snapshotter {
    private final FileHasher hasher;

    public Snapshotter(FileHasher hasher) {
        this.hasher = hasher;
    }

    public Stream<FileSnapshot> snapshot(Stream<File> fileTree) {
        return fileTree.map(file -> new FileSnapshot(file.getPath(), hasher.hash(file)));
    }
}
