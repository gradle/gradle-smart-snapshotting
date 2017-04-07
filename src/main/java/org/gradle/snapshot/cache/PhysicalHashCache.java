package org.gradle.snapshot.cache;

import com.google.common.hash.HashCode;
import org.gradle.snapshot.files.PhysicalFile;

public interface PhysicalHashCache {
    HashCode getCachedHashFor(PhysicalFile file);
    void setCachedHashFor(PhysicalFile file, HashCode hash);
}
