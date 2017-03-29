package org.gradle.snapshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class PhysicalFile implements SnapshottableFile {
    private final File file;

    public PhysicalFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public InputStream open() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getPath() {
        return file.getPath();
    }

    @Override
    public FileType getType() {
        if (!file.exists()) {
            return FileType.MISSING;
        }
        if (file.isDirectory()) {
            return FileType.DIRECTORY;
        }
        return FileType.REGULAR;
    }
}
