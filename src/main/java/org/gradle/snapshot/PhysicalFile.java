package org.gradle.snapshot;

import java.io.File;

public class PhysicalFile extends SnapshottableFile {
    private final File file;

    public PhysicalFile(File file, String relativePath) {
        super(relativePath);
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
