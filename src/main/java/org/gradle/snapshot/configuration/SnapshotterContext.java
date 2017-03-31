package org.gradle.snapshot.configuration;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.Collectors;

public class SnapshotterContext {
    private final List<SnapshotterModifier> snapshotOperations;
    private final List<ContextElement> contextElements;

    public SnapshotterContext() {
        this(ImmutableList.of(), ImmutableList.of());
    }

    private SnapshotterContext(List<SnapshotterModifier> snapshotOperations, List<ContextElement> contextElements) {
        this.snapshotOperations = snapshotOperations;
        this.contextElements = contextElements;
    }

    public List<SnapshotterModifier> getSnapshotOperations() {
        return snapshotOperations;
    }

    public SnapshotterContext withSnapshotOperations(Iterable<SnapshotterModifier> operations) {
        return new SnapshotterContext(ImmutableList.copyOf(operations), contextElements);
    }

    public SnapshotterContext withSnapshotOperation(SnapshotterModifier operation) {
        return withSnapshotOperations(ImmutableList.<SnapshotterModifier>builder().addAll(snapshotOperations).add(operation).build());
    }

    public SnapshotterContext withoutSnapshotOperation(SnapshotOperation operation) {
        return withSnapshotOperations(
                snapshotOperations.stream()
                        .filter(modifier -> !modifier.getOperation().equals(operation))
                        .collect(Collectors.toList()));
    }

    public SnapshotterContext addContextElement(ContextElement element) {
        return new SnapshotterContext(snapshotOperations,
                ImmutableList.<ContextElement>builder().addAll(contextElements).add(element).build());
    }

    public List<ContextElement> getContextElements() {
        return contextElements;
    }
}
