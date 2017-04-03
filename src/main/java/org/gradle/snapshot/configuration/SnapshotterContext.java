package org.gradle.snapshot.configuration;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class SnapshotterContext {
    private final SnapshotterConfiguration configuration;
    private final List<ContextElement> contextElements;

    public SnapshotterContext(SnapshotterConfiguration configuration) {
        this(configuration, ImmutableList.of());
    }

    private SnapshotterContext(SnapshotterConfiguration configuration, List<ContextElement> contextElements) {
        this.configuration = configuration;
        this.contextElements = contextElements;
    }

    public SnapshotterContext addContextElement(ContextElement element) {
        return new SnapshotterContext(
                configuration, ImmutableList.<ContextElement>builder().addAll(contextElements).add(element).build());
    }

    public List<ContextElement> getContextElements() {
        return contextElements;
    }

    public SnapshotterConfiguration getConfiguration() {
        return configuration;
    }
}
