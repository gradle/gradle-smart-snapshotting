package org.gradle.snapshotting.rules;

import org.gradle.snapshotting.contexts.Context;
import org.gradle.snapshotting.files.Fileish;
import org.gradle.snapshotting.operations.Operation;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public abstract class Rule<F extends Fileish, C extends Context> {
    private final Class<F> fileType;
    private final Class<C> contextType;
    private final Pattern pathMatcher;

    public Rule(Class<F> fileType, Class<C> contextType, Pattern pathMatcher) {
        this.contextType = contextType;
        this.fileType = fileType;
        this.pathMatcher = pathMatcher;
    }

    public boolean matches(Fileish file, Context context) {
        return contextType.isAssignableFrom(context.getType())
                && fileType.isAssignableFrom(file.getClass())
                && (pathMatcher == null || pathMatcher.matcher(file.getPath()).matches());
    }

    @SuppressWarnings("unchecked")
    public void process(Fileish file, Context context, List<Operation> operations) throws IOException {
        doProcess((F) file, (C) context, operations);
    }

    protected abstract void doProcess(F file, C context, List<Operation> operations) throws IOException;
}
