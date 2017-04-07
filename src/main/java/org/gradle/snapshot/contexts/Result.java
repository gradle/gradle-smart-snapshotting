package org.gradle.snapshot.contexts;

import com.google.common.hash.HashCode;
import org.gradle.snapshot.files.DefaultPhysicalSnapshot;
import org.gradle.snapshot.files.Fileish;
import org.gradle.snapshot.files.MissingPhysicalFile;
import org.gradle.snapshot.files.Physical;
import org.gradle.snapshot.files.PhysicalDirectory;
import org.gradle.snapshot.files.PhysicalSnapshot;

import java.util.Collection;

public abstract class Result {
    private final Fileish file;

    public Result(Fileish file) {
        this.file = file;
    }

    public HashCode fold(Collection<PhysicalSnapshot> physicalSnapshots) {
        HashCode hashCode = foldInternal(physicalSnapshots);
        if (file instanceof Physical) {
            HashCode physicalHash;
            if (file instanceof PhysicalDirectory) {
                physicalHash = PhysicalDirectory.HASH;
            } else if (file instanceof MissingPhysicalFile) {
                physicalHash = MissingPhysicalFile.HASH;
            } else {
                physicalHash = hashCode;
            }
            physicalSnapshots.add(new DefaultPhysicalSnapshot((Physical) file, physicalHash));
        }
        return hashCode;
    }

    public abstract HashCode foldInternal(Collection<PhysicalSnapshot> physicalSnapshots);
}
