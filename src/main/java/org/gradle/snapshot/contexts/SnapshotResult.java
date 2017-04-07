package org.gradle.snapshot.contexts;

import com.google.common.hash.HashCode;
import org.gradle.snapshot.files.Fileish;

public class SnapshotResult extends Result {
    private final HashCode hashCode;

    public SnapshotResult(Fileish file, HashCode hashCode) {
        super(file);
        this.hashCode = hashCode;
    }

    @Override
    public HashCode foldInternal(PhysicalSnapshotCollector physicalSnapshots) {
        return hashCode;
    }
}
