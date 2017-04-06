package org.gradle.snapshot.rules;

import org.gradle.snapshot.contexts.Context;
import org.gradle.snapshot.files.Fileish;
import org.gradle.snapshot.operations.Operation;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public abstract class Rule {
    private final Class<? extends Context> contextType;
    private final Class<? extends Fileish> fileType;
    private final Pattern pathMatcher;

    public Rule(Class<? extends Fileish> fileType, Class<? extends Context> contextType, Pattern pathMatcher) {
        this.contextType = contextType;
        this.fileType = fileType;
        this.pathMatcher = pathMatcher;
    }

    public boolean matches(Fileish file, Context context) {
        return contextType.isAssignableFrom(context.getType())
                && fileType.isAssignableFrom(file.getClass())
                && (pathMatcher == null || pathMatcher.matcher(file.getPath()).matches());
    }

    public abstract void process(Fileish file, Context context, List<Operation> operations) throws IOException;
}
