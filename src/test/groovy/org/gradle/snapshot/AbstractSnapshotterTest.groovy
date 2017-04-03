package org.gradle.snapshot

import io.reactivex.Observable
import org.gradle.snapshot.configuration.SnapshotOperationBindings
import org.gradle.snapshot.hashing.FileHasher
import org.gradle.snapshot.util.TestFile
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class AbstractSnapshotterTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    DefaultSnapshotter snapshotter = new DefaultSnapshotter(new FileHasher())
    SnapshotOperationBindings bindings = new SnapshotOperationBindings()

    TestFile file(Object... path) {
        return new TestFile(temporaryFolder.root, path)
    }

    List<FileSnapshot> snapshotFiles(File... files) {
        snapshotter.snapshotFiles(Observable.<File> fromArray(files), bindings).toList().blockingGet()
    }
}
