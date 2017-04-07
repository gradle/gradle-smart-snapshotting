package org.gradle.snapshot;

import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import org.gradle.snapshot.contexts.Context;
import org.gradle.snapshot.rules.Rule;

import java.util.Map;

public class SnapshotterState {
    private Context context;
    private final Iterable<? extends Rule<?, ?>> rules;
    private final Map<HashCode, HashCode> hashCache = Maps.newHashMap();

    public SnapshotterState(Context context, Iterable<? extends Rule<?, ?>> rules) {
        this.context = context;
        this.rules = rules;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Iterable<? extends Rule<?, ?>> getRules() {
        return rules;
    }
}
