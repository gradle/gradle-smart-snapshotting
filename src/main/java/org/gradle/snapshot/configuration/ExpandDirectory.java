package org.gradle.snapshot.configuration;

import org.gradle.snapshot.PhysicalFile;
import org.gradle.snapshot.SnapshottableFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ExpandDirectory implements TransformOperation {
    @Override
    public Stream<SnapshottableFile> transform(SnapshottableFile file) {
        try {
            Stream<Path> paths = Files.walk(Paths.get(file.getPath()));
            return paths.sorted().map(path -> new PhysicalFile(path.toFile()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
