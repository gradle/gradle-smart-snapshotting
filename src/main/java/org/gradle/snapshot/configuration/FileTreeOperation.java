package org.gradle.snapshot.configuration;

import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;

import java.util.stream.Stream;

public interface FileTreeOperation {
    Stream<SnapshottableFile> expand(SnapshottableFile file);

    Stream<FileSnapshot> collect(Stream<FileSnapshot> snapshots, SnapshottableFile file);
}
