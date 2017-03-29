package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;

import java.util.function.Predicate;

public class DefaultSnapshotterModifier<T> implements SnapshotterModifier<T> {
    private Predicate<SnapshottableFile> predicate;
    private T operation;

    public static <T> SnapshotterModifier<T> modifier(Predicate<SnapshottableFile> predicate, T operation) {
        return new DefaultSnapshotterModifier<>(predicate, operation);
    }

    public DefaultSnapshotterModifier(Predicate<SnapshottableFile> predicate, T operation) {
        this.predicate = predicate;
        this.operation = operation;
    }

    @Override
    public Predicate<SnapshottableFile> getPredicate() {
        return predicate;
    }

    @Override
    public T getOperation() {
        return operation;
    }
}
