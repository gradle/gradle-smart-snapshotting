package org.gradle.snapshot.contexts;

import com.google.common.hash.HashCode;
import org.gradle.snapshot.files.Fileish;
import org.gradle.snapshot.files.MissingPhysicalFile;
import org.gradle.snapshot.files.Physical;
import org.gradle.snapshot.files.PhysicalDirectory;

public abstract class Result {
    private final Fileish file;
    private final String normalizedPath;

    public Result(Fileish file, String normalizedPath) {
        this.file = file;
        this.normalizedPath = normalizedPath;
    }

    public HashCode fold(PhysicalSnapshotCollector physicalSnapshots) {
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
            physicalSnapshots.collectSnapshot((Physical) file, getNormalizedPath(), physicalHash);
        }
        return hashCode;
    }

    public abstract HashCode foldInternal(PhysicalSnapshotCollector physicalSnapshots);

    public Fileish getFile() {
        return file;
    }

    public String getNormalizedPath() {
        return normalizedPath;
    }
}
