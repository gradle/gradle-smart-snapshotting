package org.gradle.snapshot.files;

abstract public class AbstractFileish implements Fileish {
    private final String path;
    private final String relativePath;

    public AbstractFileish(String path, String relativePath) {
        this.path = path;
        this.relativePath = relativePath;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public String toString() {
        return path;
    }
}
