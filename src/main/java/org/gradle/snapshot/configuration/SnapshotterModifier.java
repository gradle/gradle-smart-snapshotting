package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;

import java.util.List;
import java.util.function.BiPredicate;

public interface SnapshotterModifier {
    BiPredicate<SnapshottableFile, List<ContextElement>> getShouldModify();
    SnapshotOperation getOperation();
}
