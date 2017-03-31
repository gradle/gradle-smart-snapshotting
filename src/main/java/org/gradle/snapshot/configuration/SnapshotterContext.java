package org.gradle.snapshot.configuration;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class SnapshotterContext {
    private final SnapshotOperationBindings bindings;
    private final List<ContextElement> contextElements;

    public SnapshotterContext() {
        this(new SnapshotOperationBindings(), ImmutableList.of());
    }

    private SnapshotterContext(SnapshotOperationBindings bindings, List<ContextElement> contextElements) {
        this.bindings = bindings;
        this.contextElements = contextElements;
    }

    public SnapshotOperationBindings getBindings() {
        return bindings;
    }

    public SnapshotterContext withBindings(SnapshotOperationBindings bindings) {
        return new SnapshotterContext(bindings, contextElements);
    }

    public SnapshotterContext addContextElement(ContextElement element) {
        return new SnapshotterContext(bindings,
                ImmutableList.<ContextElement>builder().addAll(contextElements).add(element).build());
    }

    public List<ContextElement> getContextElements() {
        return contextElements;
    }
}
