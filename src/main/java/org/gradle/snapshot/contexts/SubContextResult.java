package org.gradle.snapshot.contexts;

import com.google.common.hash.HashCode;
import org.gradle.snapshot.files.Fileish;
import org.gradle.snapshot.files.PhysicalSnapshot;

import java.util.Collection;

public class SubContextResult extends Result {
    private final Context subContext;

    public SubContextResult(Fileish file, Context subContext) {
        super(file);
        this.subContext = subContext;
    }

    public Context getSubContext() {
        return subContext;
    }

    @Override
    public HashCode foldInternal(Collection<PhysicalSnapshot> physicalSnapshots) {
        return subContext.fold(physicalSnapshots);
    }
}
