package org.gradle.snapshot;

import com.google.common.hash.HashCode;

import java.util.Comparator;

public class FileSnapshot {
    public static final Comparator<FileSnapshot> FILE_SNAPSHOT_COMPARATOR = Comparator.comparing(FileSnapshot::getPath);

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
