package org.gradle.snapshot.contexts;

import com.google.common.hash.HashCode;
import org.gradle.snapshot.files.PhysicalFileSnapshot;

import java.util.Collection;

public class SubContextResult implements Result {
    private final Context subContext;

    public SubContextResult(Context subContext) {
        this.subContext = subContext;
    }

    public Context getSubContext() {
        return subContext;
    }

    @Override
    public HashCode getHashCode() {
        return subContext.fold().getHashCode();
    }

    @Override
    public Collection<PhysicalFileSnapshot> getSnapshots() {
        return subContext.fold().getSnapshots();
    }
}
