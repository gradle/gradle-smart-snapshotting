package org.gradle.snapshot.configuration;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class SnapshotterConfiguration {
    private final List<SnapshotOperationBinding> snapshotOperationBindings;
    private final List<TransformOperationBinding> transformBindings;

    public SnapshotterConfiguration() {
        this(ImmutableList.of(), ImmutableList.of());
    }

    public SnapshotterConfiguration(List<SnapshotOperationBinding> snapshotOperationBindings, List<TransformOperationBinding> transformBindings) {
        this.snapshotOperationBindings = snapshotOperationBindings;
        this.transformBindings = transformBindings;
    }

    public List<SnapshotOperationBinding> getSnapshotOperationBindings() {
        return snapshotOperationBindings;
    }

    public List<TransformOperationBinding> getTransformBindings() {
        return transformBindings;
    }

    public SnapshotterConfiguration withSnapshotOperation(SnapshotOperationBinding binding) {
        return new SnapshotterConfiguration(
                ImmutableList.<SnapshotOperationBinding>builder()
                        .addAll(snapshotOperationBindings)
                        .add(binding)
                        .build(), transformBindings);
    }

    public SnapshotterConfiguration withTransform(TransformOperationBinding binding) {
        return new SnapshotterConfiguration(
                snapshotOperationBindings,
                ImmutableList.<TransformOperationBinding>builder()
                        .addAll(transformBindings)
                        .add(binding)
                        .build());
    }
}
