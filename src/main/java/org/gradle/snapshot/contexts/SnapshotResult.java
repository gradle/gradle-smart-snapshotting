package org.gradle.snapshot.contexts;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;
import org.gradle.snapshot.files.DefaultPhysicalFileSnapshot;
import org.gradle.snapshot.files.Fileish;
import org.gradle.snapshot.files.Physical;
import org.gradle.snapshot.files.PhysicalFileSnapshot;

import java.util.Collection;

public class SnapshotResult implements Result {
    private final HashCode hashCode;
    private final Collection<PhysicalFileSnapshot> snapshots;

    public SnapshotResult(Fileish file, HashCode hashCode) {
        this.hashCode = hashCode;
        if (file instanceof Physical) {
            snapshots = ImmutableList.of(new DefaultPhysicalFileSnapshot((Physical) file, hashCode));
        } else {
            snapshots = ImmutableList.of();
        }
    }

    public SnapshotResult(Collection<PhysicalFileSnapshot> snapshots, HashCode hashCode) {
        this.hashCode = hashCode;
        this.snapshots = snapshots;
    }

    @Override
    public HashCode getHashCode() {
        return hashCode;
    }

    @Override
    public Collection<PhysicalFileSnapshot> getSnapshots() {
        return snapshots;
    }
}
