package org.gradle.snapshot;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.hash.HashCode;
import org.gradle.snapshot.cache.PhysicalHashCache;
import org.gradle.snapshot.contexts.CachingCollector;
import org.gradle.snapshot.contexts.Context;
import org.gradle.snapshot.contexts.DefaultPhysicalSnapshotCollector;
import org.gradle.snapshot.contexts.PhysicalSnapshotCollector;
import org.gradle.snapshot.files.Fileish;
import org.gradle.snapshot.files.Physical;
import org.gradle.snapshot.files.PhysicalSnapshot;
import org.gradle.snapshot.operations.ApplyTo;
import org.gradle.snapshot.operations.Operation;
import org.gradle.snapshot.rules.Rule;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

// TODO: Handle missing files
// TODO: Handle empty directories
// TODO: Handle junk files on classpaths, and in WAR files
// TODO: Demonstrate properties file filtering
public class Snapshotter {
    private final PhysicalHashCache hashCache;

    public Snapshotter(PhysicalHashCache hashCache) {
        this.hashCache = hashCache;
    }

    public HashCode snapshot(Collection<? extends File> files, Context context, Iterable<? extends Rule<?, ?>> rules, ImmutableCollection.Builder<PhysicalSnapshot> physicalSnapshots) throws IOException {
        Collection<Physical> physicalFiles = Collections2.transform(files, file -> {
            try {
                return Physical.of(file.getAbsolutePath(), file.getName(), file);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        process(physicalFiles, context, rules, hashCache);
        PhysicalSnapshotCollector collector = new CachingCollector(
            hashCache,
            new DefaultPhysicalSnapshotCollector(physicalSnapshots)
        );
        return context.fold(collector);
    }

    private void process(Collection<? extends Fileish> files, Context rootContext, Iterable<? extends Rule<?, ?>> rules,         PhysicalHashCache hashCache) throws IOException {
        Deque<Operation> queue = Queues.newArrayDeque();
        SnapshotterState state = new SnapshotterState(rootContext, rules, hashCache);
        queue.addLast(new ApplyTo(files, rootContext));

        List<Operation> dependencies = Lists.newArrayList();

        while (!queue.isEmpty()) {
            Operation operation = queue.peek();
            operation.setContextIfNecessary(state);

            dependencies.clear();
            boolean done = operation.execute(state, dependencies);

            int dependencyCount = dependencies.size();
            if (done || dependencyCount == 0) {
                operation.close();
                queue.remove();
            }

            for (int idx = dependencyCount - 1; idx >= 0; idx--) {
                queue.push(dependencies.get(idx));
            }
        }
    }
}
