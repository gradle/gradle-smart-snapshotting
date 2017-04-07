package org.gradle.snapshot.contexts;

import com.google.common.hash.HashCode;
import org.gradle.snapshot.cache.PhysicalHashCache;
import org.gradle.snapshot.files.Physical;
import org.gradle.snapshot.files.PhysicalFile;

public class CachingCollector implements PhysicalSnapshotCollector {
    private final PhysicalHashCache hashCache;
    private final PhysicalSnapshotCollector delegate;

    public CachingCollector(PhysicalHashCache hashCache, PhysicalSnapshotCollector delegate) {
        this.hashCache = hashCache;
        this.delegate = delegate;
    }

    @Override
    public void collectSnapshot(Physical file, String normalizedPath, HashCode hashCode){
        if (file instanceof PhysicalFile) {
            hashCache.setCachedHashFor((PhysicalFile) file, hashCode);
        }
        delegate.collectSnapshot(file, normalizedPath, hashCode);
    }
}
