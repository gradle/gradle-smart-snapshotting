package org.gradle.snapshot;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;

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
            return new BufferedInputStream(Files.newInputStream(file.toPath()));
        } catch (IOException e) {
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
