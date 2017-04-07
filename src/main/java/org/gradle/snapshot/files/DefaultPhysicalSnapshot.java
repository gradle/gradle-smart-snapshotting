package org.gradle.snapshot.files;

import com.google.common.hash.HashCode;

public class DefaultPhysicalSnapshot implements PhysicalSnapshot {
    private final Physical file;
    private final HashCode hashCode;

    public DefaultPhysicalSnapshot(Physical file, HashCode hashCode) {
        this.file = file;
        this.hashCode = hashCode;
    }

    @Override
    public Physical getFile() {
        return file;
    }

    @Override
    public HashCode getHashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return file.getPath() + ": " + hashCode;
    }
}
