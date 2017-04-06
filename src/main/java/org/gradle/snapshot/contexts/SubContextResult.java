package org.gradle.snapshot.contexts;

import com.google.common.hash.HashCode;

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
        return subContext.fold();
    }
}
