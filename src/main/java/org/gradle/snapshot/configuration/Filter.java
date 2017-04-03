package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;

import java.util.stream.Stream;

public class Filter implements TransformOperation {
    @Override
    public Stream<SnapshottableFile> transform(SnapshottableFile file) {
        return Stream.empty();
    }
}
