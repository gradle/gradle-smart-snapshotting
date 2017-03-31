package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;

import java.util.List;
import java.util.function.BiPredicate;

public class DefaultSnapshotterModifier implements SnapshotterModifier {
    private BiPredicate<SnapshottableFile, List<ContextElement>> shouldModify;
    private SnapshotOperation operation;

    public static SnapshotterModifier modifier(BiPredicate<SnapshottableFile, List<ContextElement>> shouldModify, SnapshotOperation operation) {
        return new DefaultSnapshotterModifier(shouldModify, operation);
    }

    private DefaultSnapshotterModifier(BiPredicate<SnapshottableFile, List<ContextElement>> shouldModify, SnapshotOperation operation) {
        this.shouldModify = shouldModify;
        this.operation = operation;
    }

    @Override
    public BiPredicate<SnapshottableFile, List<ContextElement>> getShouldModify() {
        return shouldModify;
    }

    @Override
    public SnapshotOperation getOperation() {
        return operation;
    }
}
