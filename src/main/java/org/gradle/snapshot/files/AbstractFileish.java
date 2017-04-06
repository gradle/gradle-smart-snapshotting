package org.gradle.snapshot.files;

abstract public class AbstractFileish implements Fileish {
    private final String path;

    public AbstractFileish(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path;
    }
}
