package org.gradle.snapshot.files;

import java.io.File;

public interface Physical extends Fileish {
    File getFile();

    static Physical of(String path, String relativePath, File file) {
        if (!file.exists()) {
            return new MissingPhysicalFile(path, relativePath, file);
        } else if (file.isDirectory()) {
            return new PhysicalDirectory(path, relativePath, file);
        } else {
            return new PhysicalFile(path, relativePath, file);
        }
    }
}
