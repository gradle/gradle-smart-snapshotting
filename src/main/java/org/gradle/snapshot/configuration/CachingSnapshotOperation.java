package org.gradle.snapshot.configuration;

import com.google.common.hash.HashCode;
import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.SnapshottableFile;
import org.gradle.snapshot.Snapshotter;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class CachingSnapshotOperation implements SingleFileSnapshotOperation {
    private final SingleFileSnapshotOperation delegate;
    private final Map<HashCode, HashCode> cache;
    private final SnapshotterContext cacheKeyContext;

    public CachingSnapshotOperation(
            SingleFileSnapshotOperation delegate,
            Map<HashCode, HashCode> cache,
            SnapshotOperationBindings cacheKeyBindings) {
        this.delegate = delegate;
        this.cache = cache;
        this.cacheKeyContext = new SnapshotterContext().withBindings(cacheKeyBindings);
    }

    @Override
    public FileSnapshot snapshotSingleFile(SnapshottableFile file, SnapshotterContext context, Snapshotter snapshotter) {
        Optional<FileSnapshot> snapshotForCacheKey = snapshotter.snapshot(Stream.of(file), cacheKeyContext).findFirst();
        Optional<FileSnapshot> loadedFromCache = snapshotForCacheKey.flatMap(this::loadFromCache);

        return loadedFromCache.orElseGet(() ->
                storeResultInCache(
                        snapshotForCacheKey,
                        delegate.snapshotSingleFile(file, context, snapshotter)
                )
        );
    }

    private Optional<FileSnapshot> loadFromCache(FileSnapshot snapshotForCacheKey) {
        HashCode cacheKey = snapshotForCacheKey.getHash();
        if (cache.containsKey(cacheKey)) {
            Optional<HashCode> hashCodeFromCache = Optional.ofNullable(cache.get(cacheKey));
            return hashCodeFromCache.map(hash -> new FileSnapshot(snapshotForCacheKey.getPath(), hash));
        }
        return Optional.empty();
    }

    private FileSnapshot storeResultInCache(Optional<FileSnapshot> snapshotForCacheKey, FileSnapshot result) {
        snapshotForCacheKey.map(key ->
                cache.put(key.getHash(), result.getHash())
        );
        return result;
    }
}
