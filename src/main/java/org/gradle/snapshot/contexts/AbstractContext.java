package org.gradle.snapshot.contexts;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.gradle.snapshot.files.Fileish;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractContext implements Context {
    @VisibleForTesting
    final Map<String, Result> results = Maps.newLinkedHashMap();

    @Override
    public Class<? extends Context> getType() {
        return getClass();
    }

    @Override
    public void recordSnapshot(Fileish file, HashCode hash) {
        String path = file.getPath();
        results.put(path, new SnapshotResult(hash));
    }

    @Override
    public <C extends Context> C recordSubContext(Fileish file, Class<C> type) {
        String path = file.getPath();
        Result result = results.get(path);
        C subContext;
        if (result == null) {
            try {
                subContext = type.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
            results.put(path, new SubContextResult(subContext));
        } else if (result instanceof SubContextResult) {
            Context resultSubContext = ((SubContextResult) result).getSubContext();
            subContext = type.cast(resultSubContext);
        } else {
            throw new IllegalStateException("Already has a non-context entry under path " + path);
        }
        return subContext;
    }

    @Override
    public final HashCode fold() {
        return fold(results.entrySet());
    }

    protected HashCode fold(Collection<Map.Entry<String, Result>> results) {
        Hasher hasher = Hashing.md5().newHasher();
        results.forEach(entry -> {
            hasher.putString(entry.getKey(), Charsets.UTF_8);
            hasher.putBytes(entry.getValue().getHashCode().asBytes());
        });
        return hasher.hash();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
