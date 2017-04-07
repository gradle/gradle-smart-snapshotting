package org.gradle.snapshot.files;

import java.io.File;

public interface Physical extends Fileish {
    File getFile();

    static Physical of(String path, File file) {
        if (!file.exists()) {
            return new MissingPhysicalFile(path, file);
        } else if (file.isDirectory()) {
            return new PhysicalDirectory(path, file);
        } else {
            return new PhysicalFile(path, file);
        }
    }
}
