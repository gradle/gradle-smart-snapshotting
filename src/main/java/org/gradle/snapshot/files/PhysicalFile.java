package org.gradle.snapshot.files;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class PhysicalFile extends AbstractFileish implements FileishWithContents, Physical {
    private final File file;
    private HashCode contentHash;

    public PhysicalFile(String path, File file) {
        super(path);
        this.file = file;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public InputStream open() throws IOException {
        return new BufferedInputStream(Files.newInputStream(file.toPath()));
    }

    @Override
    public HashCode getContentHash() throws IOException {
        if (contentHash == null) {
            contentHash = com.google.common.io.Files.hash(file, Hashing.md5());
        }
        return contentHash;
    }
}
