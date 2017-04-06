package org.gradle.snapshot.contexts;

import com.google.common.hash.HashCode;
import org.gradle.snapshot.files.Fileish;

public interface Context {
    void recordSnapshot(Fileish file, HashCode hash);
    <C extends Context> C recordSubContext(Fileish file, Class<C> type);
    Class<? extends Context> getType();
    HashCode fold();
}