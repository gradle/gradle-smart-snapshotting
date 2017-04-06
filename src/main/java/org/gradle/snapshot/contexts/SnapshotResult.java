package org.gradle.snapshot.contexts;

import com.google.common.hash.HashCode;

public class SnapshotResult implements Result {
    private final HashCode hashCode;

    public SnapshotResult(HashCode hashCode) {
        this.hashCode = hashCode;
    }

    @Override
    public HashCode getHashCode() {
        return hashCode;
    }
}
