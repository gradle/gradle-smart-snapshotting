package org.gradle.snapshot.contexts;

import com.google.common.hash.HashCode;
import org.gradle.snapshot.files.Fileish;
import org.gradle.snapshot.files.FileishWithContents;

public interface Context {
    void recordSnapshot(Fileish file, HashCode hash);
    <C extends Context> C recordSubContext(Fileish file, Class<C> type);
    Class<? extends Context> getType();
    SnapshotResult fold();
    void recordOriginFile(FileishWithContents file);
}
