package org.gradle.snapshot;

public class SnapshottableFile {
    private String path;

    public SnapshottableFile(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
