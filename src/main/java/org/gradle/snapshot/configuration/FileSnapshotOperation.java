package org.gradle.snapshot.configuration;

import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;
import org.gradle.snapshot.Snapshotter;

import java.util.stream.Stream;

@FunctionalInterface
public interface FileSnapshotOperation extends SnapshotOperation {
    @Override
    default Stream<FileSnapshot> snapshot(SnapshottableFile file, SnapshotterContext context, Snapshotter snapshotter) {
        return snapshotter.snapshot(Stream.of(transform(file)), context.withBindings(context.getBindings().withoutSnapshotOperation(this)));
    }

    SnapshottableFile transform(SnapshottableFile snapshottableFile);
}
