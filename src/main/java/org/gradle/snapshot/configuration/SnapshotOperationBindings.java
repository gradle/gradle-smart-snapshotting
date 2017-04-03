package org.gradle.snapshot.configuration;

import com.google.common.collect.ImmutableList;
import io.reactivex.Observable;

import java.util.AbstractList;
import java.util.List;

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
                Observable.fromIterable(bindings)
                        .filter(modifier -> !modifier.getOperation().equals(operation))
                        .toList().blockingGet());
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
