package org.gradle.snapshot.operations;

import org.gradle.snapshot.SnapshotterState;
import org.gradle.snapshot.contexts.Context;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public abstract class Operation implements Closeable {
    private Context context;

    public Operation(Context context) {
        this.context = context;
    }

    public Context getContext() {
        if (context == null) {
            throw new IllegalStateException("No context is specified");
        }
        return context;
    }

    public void setContextIfNecessary(SnapshotterState state) {
        if (this.context == null) {
            this.context = state.getContext();
        } else {
            state.setContext(this.context);
        }
    }

    public abstract boolean execute(SnapshotterState state, List<Operation> dependencies) throws IOException;

    @Override
    public void close() throws IOException {
    }
}
