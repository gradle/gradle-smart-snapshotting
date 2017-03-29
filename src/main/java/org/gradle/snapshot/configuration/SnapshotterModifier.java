package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;

import java.util.function.Predicate;

public interface SnapshotterModifier<T> {
    Predicate<SnapshottableFile> getPredicate();
    T getOperation();
}
