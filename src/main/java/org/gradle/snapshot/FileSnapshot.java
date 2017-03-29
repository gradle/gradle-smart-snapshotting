package org.gradle.snapshot;

import com.google.common.hash.HashCode;

public class FileSnapshot {
    private String path;
    private HashCode hash;

    public FileSnapshot(String path, HashCode hash) {
        this.path = path;
        this.hash = hash;
    }

    public String getPath() {
        return path;
    }

    public HashCode getHash() {
        return hash;
    }
}
