package org.gradle.snapshot.configuration;

import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.PhysicalFile;
import org.gradle.snapshot.SnapshottableFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ExpandDirectory implements FileTreeOperation {
    @Override
    public Stream<SnapshottableFile> expand(SnapshottableFile file) {
        try {
            Stream<Path> paths = Files.walk(Paths.get(file.getPath()));
            return paths.map(path -> new PhysicalFile(path.toFile()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Stream<FileSnapshot> collect(Stream<FileSnapshot> snapshots, SnapshottableFile file) {
        return snapshots.sorted();
    }
}
