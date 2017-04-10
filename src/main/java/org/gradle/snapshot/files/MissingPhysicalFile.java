package org.gradle.snapshot.files;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import java.io.File;

public class MissingPhysicalFile extends AbstractFileish implements Physical {
    public static final HashCode HASH = Hashing.md5().hashString("MISSING_FILE", Charsets.UTF_8);
    private final File file;

    public MissingPhysicalFile(String path, String relativePath, File file) {
        super(path, relativePath);
        this.file = file;
    }

    @Override
    public File getFile() {
        return file;
    }
}
