package org.gradle.snapshot;

import java.io.InputStream;

public interface SnapshottableFile {
    /**
     * Opens the input stream. This is only guaranteed to work once.
     * The caller needs to make sure to close the input stream.
     */
    InputStream open();
    String getPath();
    FileType getType();
}
