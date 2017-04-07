package org.gradle.snapshot;

import org.gradle.snapshot.cache.PhysicalHashCache;
import org.gradle.snapshot.contexts.Context;
import org.gradle.snapshot.rules.Rule;

public class SnapshotterState {
    private Context context;
    private final Iterable<? extends Rule<?, ?>> rules;
    private final PhysicalHashCache hashCache;

    public SnapshotterState(Context context, Iterable<? extends Rule<?, ?>> rules, PhysicalHashCache hashCache) {
        this.context = context;
        this.rules = rules;
        this.hashCache = hashCache;
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

    public PhysicalHashCache getHashCache() {
        return hashCache;
    }
}
