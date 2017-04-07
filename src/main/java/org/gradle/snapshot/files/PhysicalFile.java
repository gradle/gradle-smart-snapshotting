package org.gradle.snapshot.files;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PhysicalFile extends AbstractFileish implements Physical, FileishWithContents {
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
        return new FileInputStream(file);
    }

    @Override
    public HashCode getContentHash() throws IOException {
        if (contentHash == null) {
            contentHash = Files.hash(file, Hashing.md5());
        }
        return contentHash;
    }
}
