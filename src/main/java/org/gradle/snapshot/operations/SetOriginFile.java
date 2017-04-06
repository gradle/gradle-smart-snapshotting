package org.gradle.snapshot.operations;

import org.gradle.snapshot.SnapshotterState;
import org.gradle.snapshot.contexts.Context;
import org.gradle.snapshot.files.FileishWithContents;

import java.io.IOException;
import java.util.List;

public class SetOriginFile extends Operation {
    private final FileishWithContents file;

    public SetOriginFile(FileishWithContents file, Context context) {
        super(context);
        this.file = file;
    }

    @Override
    public boolean execute(SnapshotterState state, List<Operation> dependencies) throws IOException {
        getContext().recordOriginFile(file);
        return true;
    }
}
