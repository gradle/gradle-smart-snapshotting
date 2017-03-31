package org.gradle.snapshot

import org.gradle.snapshot.configuration.SnapshotOperationBindings
import org.gradle.snapshot.hashing.FileHasher
import org.gradle.snapshot.util.TestFile
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.stream.Collectors
import java.util.stream.Stream

class AbstractSnapshotterTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    DefaultSnapshotter snapshotter = new DefaultSnapshotter(new FileHasher())
    SnapshotOperationBindings bindings = new SnapshotOperationBindings()

    TestFile file(Object... path) {
        return new TestFile(temporaryFolder.root, path)
    }

    List<FileSnapshot> snapshotFiles(File... files) {
        snapshotter.snapshotFiles(Stream.<File> of(files), bindings).collect(Collectors.toList())
    }
}
