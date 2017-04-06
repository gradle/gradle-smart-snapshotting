package org.gradle.snapshot.operations;

import org.gradle.snapshot.SnapshotterState;
import org.gradle.snapshot.contexts.Context;

import java.io.IOException;
import java.util.List;

public class SetContext extends Operation {
    public SetContext(Context context) {
        super(context);
    }

    @Override
    public boolean execute(SnapshotterState state, List<Operation> dependencies) throws IOException {
        state.setContext(getContext());
        return true;
    }
}
