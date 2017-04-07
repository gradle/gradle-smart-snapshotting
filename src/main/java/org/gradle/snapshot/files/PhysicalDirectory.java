package org.gradle.snapshot.files;

import java.io.File;

public class PhysicalDirectory extends AbstractFileish implements Physical, Directoryish {
    private final File file;

    public PhysicalDirectory(String path, String relativePath, File file) {
        super(path, relativePath);
        this.file = file;
    }

    @Override
    public File getFile() {
        return file;
    }
}
