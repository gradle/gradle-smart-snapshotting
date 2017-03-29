package org.gradle.snapshot.configuration;

import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;

import java.io.File;
import java.util.stream.Collector;
import java.util.stream.Stream;

public interface FileTreeOperation {
    boolean applies(File file);
    Stream<SnapshottableFile> expand(File file);
    Collector<FileSnapshot, ?, FileSnapshot> collector(File file);
}
