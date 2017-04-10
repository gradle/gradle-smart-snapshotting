package org.gradle.snapshot.contexts;

import com.google.common.hash.HashCode;
import org.gradle.snapshot.files.Fileish;

public class SubContextResult extends Result {
    private final Context subContext;

    public SubContextResult(Fileish file, String normalizedPath, Context subContext) {
        super(file, normalizedPath);
        this.subContext = subContext;
    }

    public Context getSubContext() {
        return subContext;
    }

    @Override
    public HashCode foldInternal(PhysicalSnapshotCollector physicalSnapshots) {
        return subContext.fold(physicalSnapshots);
    }
}
