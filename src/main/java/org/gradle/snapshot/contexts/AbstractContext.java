package org.gradle.snapshot.contexts;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.gradle.snapshot.files.DefaultPhysicalFileSnapshot;
import org.gradle.snapshot.files.Fileish;
import org.gradle.snapshot.files.FileishWithContents;
import org.gradle.snapshot.files.PhysicalFile;
import org.gradle.snapshot.files.PhysicalFileSnapshot;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractContext implements Context {
    @VisibleForTesting
    final Map<String, Result> results = Maps.newLinkedHashMap();
    private PhysicalFile originFile;

    @Override
    public Class<? extends Context> getType() {
        return getClass();
    }

    @Override
    public void recordSnapshot(Fileish file, HashCode hash) {
        results.put(file.getPath(), new SnapshotResult(file, hash));
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
            final SubContextResult subContextResult = new SubContextResult(subContext);
            results.put(path, subContextResult);
        } else if (result instanceof SubContextResult) {
            Context resultSubContext = ((SubContextResult) result).getSubContext();
            subContext = type.cast(resultSubContext);
        } else {
            throw new IllegalStateException("Already has a non-context entry under path " + path);
        }
        return subContext;
    }

    @Override
    public final SnapshotResult fold() {
        return fold(results.entrySet());
    }

    protected SnapshotResult fold(Collection<Map.Entry<String, Result>> results) {
        Hasher hasher = Hashing.md5().newHasher();
        ImmutableList.Builder<PhysicalFileSnapshot> builder = ImmutableList.builder();
        results.forEach(entry -> {
            hasher.putString(entry.getKey(), Charsets.UTF_8);
            hasher.putBytes(entry.getValue().getHashCode().asBytes());
            builder.addAll(entry.getValue().getSnapshots());
        });
        HashCode foldedHash = hasher.hash();
        if (originFile != null) {
            builder.add(new DefaultPhysicalFileSnapshot(originFile, foldedHash));
        }
        return new SnapshotResult(builder.build(), foldedHash);
    }

    @Override
    public void recordOriginFile(FileishWithContents file) {
        if (file instanceof PhysicalFile) {
            if (originFile != null) {
                throw new IllegalStateException("Only can set one physical origin file");
            }
            originFile = (PhysicalFile) file;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
