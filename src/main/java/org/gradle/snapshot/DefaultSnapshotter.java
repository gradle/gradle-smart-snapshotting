package org.gradle.snapshot;

import com.google.common.base.Preconditions;
import org.gradle.snapshot.configuration.OperationBinding;
import org.gradle.snapshot.opeartion.snapshot.SnapshotOperation;
import org.gradle.snapshot.configuration.SnapshotterConfiguration;
import org.gradle.snapshot.configuration.SnapshotterContext;
import org.gradle.snapshot.opeartion.transform.TransformOperation;
import org.gradle.snapshot.hashing.FileHasher;

import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultSnapshotter implements Snapshotter {
    private final FileHasher hasher;

    public DefaultSnapshotter(FileHasher hasher) {
        this.hasher = hasher;
    }

    public Stream<FileSnapshot> snapshotFiles(Stream<? extends File> fileTree, SnapshotterConfiguration configuration) {
        return snapshot(fileTree.map(PhysicalFile::new), new SnapshotterContext(configuration));
    }

    public Stream<FileSnapshot> snapshot(Stream<SnapshottableFile> fileTree, SnapshotterContext context) {
        return fileTree.flatMap(applyOperations(context));
    }

    private Function<SnapshottableFile, Stream<? extends FileSnapshot>> applyOperations(SnapshotterContext context) {
        return file -> applyOperations(context, file);
    }

    private Stream<FileSnapshot> applyOperations(SnapshotterContext context, SnapshottableFile file) {
        Stream<TransformOperation> transforms = context.getConfiguration().getTransformBindings().stream()
                .filter(op -> op.getBoundTo().test(file, context.getContextElements()))
                .map(OperationBinding::getOperation);

        Stream<SnapshottableFile> transformedFiles = transforms.reduce(
                Stream.of(file),
                (snFile, op) -> snFile.flatMap(op::transform),
                Stream::concat);

        List<SnapshotOperation> operations = context.getConfiguration().getSnapshotOperationBindings().stream()
                .filter(op -> op.getBoundTo().test(file, context.getContextElements()))
                .map(OperationBinding::getOperation)
                .collect(Collectors.toList());

        Preconditions.checkState(
                operations.size() <= 1,
                "It is not possible to have more than one snapshotting transform per file.");

        if (operations.isEmpty()) {
            return transformedFiles.map(this::snapshotFile);
        }
        return transformedFiles.flatMap(snF -> operations.iterator().next().snapshot(snF, context, this));
    }

    private FileSnapshot snapshotFile(SnapshottableFile file) {
        return new FileSnapshot(file.getPath(), hasher.hash(file));
    }


}
