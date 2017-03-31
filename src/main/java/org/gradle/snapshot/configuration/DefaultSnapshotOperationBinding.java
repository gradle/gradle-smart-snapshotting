package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;

import java.util.List;
import java.util.function.BiPredicate;

public class DefaultSnapshotOperationBinding implements SnapshotOperationBinding {
    private BiPredicate<SnapshottableFile, List<ContextElement>> boundTo;
    private SnapshotOperation operation;

    public static SnapshotOperationBinding binding(SnapshotOperation operation, BiPredicate<SnapshottableFile, List<ContextElement>> boundTo) {
        return new DefaultSnapshotOperationBinding(operation, boundTo);
    }

    private DefaultSnapshotOperationBinding(SnapshotOperation operation, BiPredicate<SnapshottableFile, List<ContextElement>> boundTo) {
        this.operation = operation;
        this.boundTo = boundTo;
    }

    @Override
    public BiPredicate<SnapshottableFile, List<ContextElement>> getBoundTo() {
        return boundTo;
    }

    @Override
    public SnapshotOperation getOperation() {
        return operation;
    }
}
