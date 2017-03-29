package org.gradle.snapshot;

import java.io.InputStream;

public interface SnapshottableFile {
    InputStream open();
    String getPath();
    FileType getType();
}
