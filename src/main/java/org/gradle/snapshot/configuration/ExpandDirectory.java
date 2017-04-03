package org.gradle.snapshot.configuration;

import io.reactivex.Observable;
import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.PhysicalFile;
import org.gradle.snapshot.SnapshottableFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.gradle.snapshot.FileSnapshot.FILE_SNAPSHOT_COMPARATOR;

public class ExpandDirectory implements FileTreeOperation {
    @Override
    public Observable<SnapshottableFile> expand(SnapshottableFile file) {
        try {
            // We are still using the Stream API here, but that should be fine since we will replace
            // this call by our own file walking anyway.
            Collection<Path> paths = Files.walk(Paths.get(file.getPath())).collect(Collectors.toList());
            return Observable.fromIterable(paths).map(path -> new PhysicalFile(path.toFile()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Observable<FileSnapshot> collect(Observable<FileSnapshot> snapshots, SnapshottableFile file) {
        return snapshots.sorted(FILE_SNAPSHOT_COMPARATOR);
    }
}
