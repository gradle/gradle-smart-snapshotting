package org.gradle.snapshot;

import java.util.stream.Stream;

public class FileTree {
    private final Stream<SnapshottableFile> files;

    public FileTree(Stream<SnapshottableFile> files) {
        this.files = files;
    }

    public Stream<SnapshottableFile> getFiles() {
        return files;
    }
}
