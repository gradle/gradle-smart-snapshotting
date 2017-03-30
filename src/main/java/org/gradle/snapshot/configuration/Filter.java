package org.gradle.snapshot.configuration;

import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;

import java.util.stream.Stream;

public class Filter implements FileTreeOperation {
    @Override
    public Stream<SnapshottableFile> expand(SnapshottableFile file) {
        return Stream.empty();
    }

    @Override
    public Stream<FileSnapshot> collect(Stream<FileSnapshot> snapshots, SnapshottableFile file) {
        return snapshots;
    }
}
