package org.gradle.snapshot.contexts;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.gradle.snapshot.files.DefaultPhysicalSnapshot;
import org.gradle.snapshot.files.Fileish;
import org.gradle.snapshot.files.Physical;
import org.gradle.snapshot.files.PhysicalDirectory;
import org.gradle.snapshot.files.PhysicalSnapshot;

import java.util.Collection;

public abstract class Result {
    public static final HashCode EMPTY_DIRECTORY_HASH = Hashing.md5().hashString("EMPTY_DIRECTORY_HASH", Charsets.UTF_8);
    private final Fileish file;

    public Result(Fileish file) {
        this.file = file;
    }

    public HashCode fold(Collection<PhysicalSnapshot> physicalSnapshots) {
        HashCode hashCode = foldInternal(physicalSnapshots);
        if (file instanceof Physical) {
            HashCode physicalHash;
            if (file instanceof PhysicalDirectory) {
                physicalHash = EMPTY_DIRECTORY_HASH;
            } else {
                physicalHash = hashCode;
            }
            physicalSnapshots.add(new DefaultPhysicalSnapshot((Physical) file, physicalHash));
        }
        return hashCode;
    }

    public abstract HashCode foldInternal(Collection<PhysicalSnapshot> physicalSnapshots);
}
