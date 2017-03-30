package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;

import java.util.List;
import java.util.function.Predicate;

public interface SnapshotterModifier<T> {
    Predicate<SnapshottableFile> getFilePredicate();
    Predicate<List<ContextElement>> getContextPredicate();
    T getOperation();
}
