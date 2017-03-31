package org.gradle.snapshot.configuration;

import com.google.common.collect.ImmutableList;

import java.util.AbstractList;
import java.util.List;
import java.util.stream.Collectors;

public class SnapshotOperationBindings extends AbstractList<SnapshotOperationBinding> {
    private final List<SnapshotOperationBinding> bindings;

    public SnapshotOperationBindings() {
        bindings = ImmutableList.of();
    }

    public SnapshotOperationBindings(List<SnapshotOperationBinding> bindings) {
        this.bindings = bindings;
    }

    public List<SnapshotOperationBinding> getBindings() {
        return bindings;
    }

    public SnapshotOperationBindings withBindings(Iterable<SnapshotOperationBinding> operations) {
        return new SnapshotOperationBindings(ImmutableList.copyOf(operations));
    }

    public SnapshotOperationBindings withBinding(SnapshotOperationBinding binding) {
        return withBindings(ImmutableList.<SnapshotOperationBinding>builder().addAll(bindings).add(binding).build());
    }

    public SnapshotOperationBindings withoutSnapshotOperation(SnapshotOperation operation) {
        return withBindings(
                bindings.stream()
                        .filter(modifier -> !modifier.getOperation().equals(operation))
                        .collect(Collectors.toList()));
    }

    @Override
    public SnapshotOperationBinding get(int index) {
        return bindings.get(index);
    }

    @Override
    public int size() {
        return bindings.size();
    }
}
