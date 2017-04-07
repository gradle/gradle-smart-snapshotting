package org.gradle.snapshot.rules;

import org.gradle.snapshot.contexts.Context;
import org.gradle.snapshot.files.Directoryish;
import org.gradle.snapshot.files.Fileish;
import org.gradle.snapshot.operations.Operation;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public abstract class DirectoryRule extends Rule {
    public DirectoryRule(Class<? extends Context> contextType, Pattern pathMatcher) {
        super(Directoryish.class, contextType, pathMatcher);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Fileish file, Context context, List<Operation> operations) throws IOException {
        processDirectory((Directoryish) file, context, operations);
    }

    abstract protected void processDirectory(Directoryish directory, Context context, List<Operation> operations) throws IOException;
}
