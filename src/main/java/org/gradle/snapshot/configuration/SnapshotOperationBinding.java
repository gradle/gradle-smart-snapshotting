package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;

import java.util.List;
import java.util.function.BiPredicate;

public interface SnapshotOperationBinding {
    SnapshotOperation getOperation();
    BiPredicate<SnapshottableFile, List<ContextElement>> getBoundTo();
}
