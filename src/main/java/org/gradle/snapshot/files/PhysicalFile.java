package org.gradle.snapshot.files;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;

public class PhysicalFile extends AbstractFileish implements FileishWithContents, Physical {
    private final File file;

    public PhysicalFile(String path, File file) {
        super(path);
        this.file = file;
    }

    @Override
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
}
