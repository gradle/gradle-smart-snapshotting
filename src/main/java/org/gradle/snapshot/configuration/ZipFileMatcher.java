package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;

import java.util.function.Predicate;

public class ZipFileMatcher implements Predicate<SnapshottableFile> {
    public static final Predicate<SnapshottableFile> IS_ZIP_FILE = new ZipFileMatcher();

    @Override
    public boolean test(SnapshottableFile file) {
        return file.getPath().endsWith(".zip");
    }
}
