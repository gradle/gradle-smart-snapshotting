package org.gradle.snapshot.configuration;

import ix.Ix;
import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;

public class Filter implements FileTreeOperation {
    @Override
    public Ix<SnapshottableFile> expand(SnapshottableFile file) {
        return Ix.empty();
    }

    @Override
    public Ix<FileSnapshot> collect(Ix<FileSnapshot> snapshots, SnapshottableFile file) {
        return snapshots;
    }
}
