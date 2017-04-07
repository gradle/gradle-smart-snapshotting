package org.gradle.snapshot.rules;

import org.gradle.snapshot.contexts.Context;
import org.gradle.snapshot.files.Fileish;
import org.gradle.snapshot.files.FileishWithContents;
import org.gradle.snapshot.operations.Operation;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public abstract class ContentRule extends Rule {
    public ContentRule(Class<? extends Context> contextType, Pattern pathMatcher) {
        super(FileishWithContents.class, contextType, pathMatcher);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Fileish file, Context context, List<Operation> operations) throws IOException {
        processContents((FileishWithContents) file, context, operations);
    }

    abstract protected void processContents(FileishWithContents file, Context context, List<Operation> operations) throws IOException;
}
