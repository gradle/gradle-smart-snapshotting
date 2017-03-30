package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;

public interface FileSnapshotOperation {
    SnapshottableFile transform(SnapshottableFile snapshottableFile);
}
