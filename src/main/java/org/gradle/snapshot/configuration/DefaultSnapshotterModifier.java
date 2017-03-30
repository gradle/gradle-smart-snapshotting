package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;

import java.util.List;
import java.util.function.Predicate;

public class DefaultSnapshotterModifier<T> implements SnapshotterModifier<T> {
    private Predicate<SnapshottableFile> filePredicate;
    private Predicate<List<ContextElement>> contextPredicate;
    private T operation;

    public static <T> SnapshotterModifier<T> modifier(Predicate<SnapshottableFile> predicate, T operation) {
        return new DefaultSnapshotterModifier<>(it -> true, predicate, operation);
    }

    public static <T> SnapshotterModifier<T> modifier(Predicate<List<ContextElement>> contextPredicate, Predicate<SnapshottableFile> predicate, T operation) {
        return new DefaultSnapshotterModifier<>(contextPredicate, predicate, operation);
    }

    public DefaultSnapshotterModifier(Predicate<List<ContextElement>> contextPredicate, Predicate<SnapshottableFile> filePredicate, T operation) {
        this.contextPredicate = contextPredicate;
        this.filePredicate = filePredicate;
        this.operation = operation;
    }

    @Override
    public Predicate<SnapshottableFile> getFilePredicate() {
        return filePredicate;
    }

    @Override
    public Predicate<List<ContextElement>> getContextPredicate() {
        return contextPredicate;
    }

    @Override
    public T getOperation() {
        return operation;
    }
}
