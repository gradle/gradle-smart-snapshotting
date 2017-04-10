package org.gradle.snapshot.files;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

public interface Physical extends Fileish {
    File getFile();

    static Physical of(String path, String relativePath, File file) {
        try {
            if (!file.exists()) {
                return new MissingPhysicalFile(path, relativePath, file);
            } else if (file.isDirectory()) {
                return new PhysicalDirectory(path, relativePath, file);
            } else {
                HashCode contentHash = Files.hash(file, Hashing.md5());
                return new PhysicalFile(path, relativePath, file, contentHash);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
