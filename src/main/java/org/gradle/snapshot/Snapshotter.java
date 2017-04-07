package org.gradle.snapshot;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.gradle.snapshot.contexts.Context;
import org.gradle.snapshot.files.Fileish;
import org.gradle.snapshot.files.Physical;
import org.gradle.snapshot.operations.ApplyTo;
import org.gradle.snapshot.operations.Operation;
import org.gradle.snapshot.rules.Rule;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

// TODO: Handle missing files
// TODO: Handle empty directories
// TODO: Handle junk files on classpaths, and in WAR files
// TODO: Demonstrate properties file filtering
public class Snapshotter {
    public <C extends Context> C snapshot(Collection<? extends File> files, C context, Iterable<? extends Rule<?, ?>> rules) throws IOException {
        process(files.stream()
                .map(file -> Physical.of(file.getName(), file))
                .collect(Collectors.toList()), context, rules);
        return context;
    }

    private void process(Collection<? extends Fileish> files, Context rootContext, Iterable<? extends Rule<?, ?>> rules) throws IOException {
        Deque<Operation> queue = Queues.newArrayDeque();
        SnapshotterState state = new SnapshotterState(rootContext, rules);
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
