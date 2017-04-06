package org.gradle.snapshot;

import org.gradle.snapshot.contexts.Context;
import org.gradle.snapshot.rules.Rule;

public class SnapshotterState {
    private Context context;
    private final Iterable<? extends Rule> rules;

    public SnapshotterState(Context context, Iterable<? extends Rule> rules) {
        this.context = context;
        this.rules = rules;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Iterable<? extends Rule> getRules() {
        return rules;
    }
}
