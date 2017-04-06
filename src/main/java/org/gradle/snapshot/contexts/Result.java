package org.gradle.snapshot.contexts;

import com.google.common.hash.HashCode;
import org.gradle.snapshot.files.PhysicalFileSnapshot;

import java.util.Collection;

public interface Result {
    HashCode getHashCode();
    Collection<PhysicalFileSnapshot> getSnapshots();
}
