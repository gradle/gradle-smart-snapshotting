package org.gradle.snapshot.contexts;

import com.google.common.hash.HashCode;
import org.gradle.snapshot.files.Physical;

public interface PhysicalSnapshotCollector {
    void collectSnapshot(Physical file, String normalizedPath, HashCode hashCode);
}
