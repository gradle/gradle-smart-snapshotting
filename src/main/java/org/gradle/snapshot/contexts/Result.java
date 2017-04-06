package org.gradle.snapshot.contexts;

import com.google.common.hash.HashCode;

public interface Result {
    HashCode getHashCode();
}
