package org.gradle.snapshot.configuration;

import org.gradle.snapshot.SnapshottableFile;

import java.util.List;
import java.util.function.BiPredicate;

public class ZipFileMatcher implements BiPredicate<SnapshottableFile, List<ContextElement>> {
    public static final BiPredicate<SnapshottableFile, List<ContextElement>> IS_ZIP_FILE = new ZipFileMatcher();

    @Override
    public boolean test(SnapshottableFile file, List<ContextElement> contextElements) {
        return file.getPath().endsWith(".zip");
    }
}
