package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;
import org.gradle.snapshot.opeartion.snapshot.SnapshotOperation;
import org.gradle.snapshot.opeartion.transform.TransformOperation;

import java.util.List;
import java.util.function.BiPredicate;

public class DefaultOperationBinding<T> implements OperationBinding<T> {
    private BiPredicate<SnapshottableFile, List<ContextElement>> boundTo;
    private T operation;

    public static SnapshotOperationBinding binding(SnapshotOperation operation, BiPredicate<SnapshottableFile, List<ContextElement>> boundTo) {
        return new DefaultSnapshotOperationBinding(operation, boundTo) {};
    }

    public static TransformOperationBinding binding(TransformOperation operation, BiPredicate<SnapshottableFile, List<ContextElement>> boundTo) {
        return new DefaultTransformOperationBinding(operation, boundTo) {};
    }

    private DefaultOperationBinding(T operation, BiPredicate<SnapshottableFile, List<ContextElement>> boundTo) {
        this.operation = operation;
        this.boundTo = boundTo;
    }

    @Override
    public BiPredicate<SnapshottableFile, List<ContextElement>> getBoundTo() {
        return boundTo;
    }

    @Override
    public T getOperation() {
        return operation;
    }

    private static class DefaultSnapshotOperationBinding extends DefaultOperationBinding<SnapshotOperation> implements SnapshotOperationBinding {
        private DefaultSnapshotOperationBinding(SnapshotOperation operation, BiPredicate<SnapshottableFile, List<ContextElement>> boundTo) {
            super(operation, boundTo);
        }
    }

    private static class DefaultTransformOperationBinding extends DefaultOperationBinding<TransformOperation> implements TransformOperationBinding {
        private DefaultTransformOperationBinding(TransformOperation operation, BiPredicate<SnapshottableFile, List<ContextElement>> boundTo) {
            super(operation, boundTo);
        }
    }
}
