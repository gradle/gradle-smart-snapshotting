package org.gradle.snapshotting.operations;

import com.google.common.hash.HashCode;
import org.gradle.snapshotting.SnapshotterState;
import org.gradle.snapshotting.contexts.Context;
import org.gradle.snapshotting.files.Fileish;
import org.gradle.snapshotting.files.PhysicalFile;
import org.gradle.snapshotting.rules.Rule;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ApplyTo extends Operation {
    private final Iterable<? extends Fileish> files;

    public ApplyTo(Fileish file) {
        this(Collections.singleton(file), null);
    }

    public ApplyTo(Iterable<? extends Fileish> files, Context context) {
        super(context);
        this.files = files;
    }

    @Override
    public boolean execute(SnapshotterState state, List<Operation> dependencies) throws IOException {
        Context context = getContext();
        for (Fileish file : files) {
            if (file instanceof PhysicalFile) {
                HashCode cachedHash = state.getHashCache().getCachedHashFor((PhysicalFile) file);
                if (cachedHash != null) {
                    getContext().recordSnapshot(file, cachedHash);
                    continue;
                }
            }

            Rule<?, ?> matchedRule = null;
            for (Rule<?, ?> rule : state.getRules()) {
                if (rule.matches(file, context)) {
                    matchedRule = rule;
                    break;
                }
            }
            if (matchedRule == null) {
                throw new IllegalStateException(String.format("Cannot find matching rule for %s in context %s", file, context));
            }
            matchedRule.process(file, context, dependencies);
        }
        return true;
    }
}
