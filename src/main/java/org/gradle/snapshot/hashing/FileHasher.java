package org.gradle.snapshot.hashing;

import com.google.common.hash.Funnels;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

public class FileHasher {
    public static final HashCode DIRECTORY_HASH = createHasher().putUnencodedChars("IS_DIRECTORY").hash();
    public static final HashCode MISSING_FILE_HASH = createHasher().putUnencodedChars("IS_MISSING").hash();

    public HashCode hash(File file) {
        if (!file.exists()) {
            return MISSING_FILE_HASH;
        }
        if (file.isDirectory()) {
            return DIRECTORY_HASH;
        }
        Hasher hasher = createHasher();
        try {
            Files.copy(file.toPath(), Funnels.asOutputStream(hasher));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return hasher.hash();
    }

    private static Hasher createHasher() {
        return Hashing.md5().newHasher();
    }
}
