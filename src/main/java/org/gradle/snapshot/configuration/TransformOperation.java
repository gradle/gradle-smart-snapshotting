package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;

import java.util.stream.Stream;

@FunctionalInterface
public interface TransformOperation {
    Stream<SnapshottableFile> transform(SnapshottableFile file);
}
