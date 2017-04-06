package org.gradle.snapshot.files;

import com.google.common.hash.HashCode;

public class DefaultPhysicalFileSnapshot implements PhysicalFileSnapshot {
    private final Physical file;
    private final HashCode hashCode;

    public DefaultPhysicalFileSnapshot(Physical file, HashCode hashCode) {
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
}
