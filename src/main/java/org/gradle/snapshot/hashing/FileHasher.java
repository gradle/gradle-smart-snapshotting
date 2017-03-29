package org.gradle.snapshot.hashing;

import com.google.common.hash.Funnels;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import org.gradle.snapshot.SnapshottableFile;

import java.io.IOException;
import java.io.UncheckedIOException;

public class FileHasher {
    public static final HashCode DIRECTORY_HASH = createHasher().putUnencodedChars("IS_DIRECTORY").hash();
    public static final HashCode MISSING_FILE_HASH = createHasher().putUnencodedChars("IS_MISSING").hash();

    public HashCode hash(SnapshottableFile snapshottableFile) {
        switch (snapshottableFile.getType()) {
            case MISSING:
                return MISSING_FILE_HASH;
            case DIRECTORY:
                return DIRECTORY_HASH;
            case REGULAR:
                try {
                    Hasher hasher = createHasher();
                    ByteStreams.copy(snapshottableFile.open(), Funnels.asOutputStream(hasher));
                    return hasher.hash();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            default:
                throw new IllegalStateException();
        }
    }

    private static Hasher createHasher() {
        return Hashing.md5().newHasher();
    }
}
