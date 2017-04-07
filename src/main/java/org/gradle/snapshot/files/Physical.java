package org.gradle.snapshot.files;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

public interface Physical extends Fileish {
    File getFile();

    static Physical of(String path, File file) throws IOException {
        if (!file.exists()) {
            return new MissingPhysicalFile(path, file);
        } else if (file.isDirectory()) {
            return new PhysicalDirectory(path, file);
        } else {
            HashCode contentHash = Files.hash(file, Hashing.md5());
            return new PhysicalFile(path, file, contentHash);
        }
    }
}
