package org.gradle.snapshot.files;

import com.google.common.hash.HashCode;

public interface PhysicalFileSnapshot {
    Physical getFile();
    HashCode getHashCode();
}
