package org.gradle.snapshot.rules;

import org.gradle.snapshot.contexts.Context;
import org.gradle.snapshot.files.FileishWithContents;
import org.gradle.snapshot.operations.Operation;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class SnapshotFileRule extends FileRule {
    public SnapshotFileRule(Class<? extends Context> contextType, Pattern pathMatcher) {
        super(contextType, pathMatcher);
    }

    @Override
    protected void processContents(FileishWithContents file, Context context, List<Operation> operations) throws IOException {
        context.recordSnapshot(file, file.getContentHash());
    }
}
