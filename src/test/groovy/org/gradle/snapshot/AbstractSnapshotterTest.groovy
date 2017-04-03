package org.gradle.snapshot

import org.gradle.snapshot.configuration.SnapshotterConfiguration
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
    SnapshotterConfiguration configuration = new SnapshotterConfiguration()

    TestFile file(Object... path) {
        return new TestFile(temporaryFolder.root, path)
    }

    List<FileSnapshot> snapshotFiles(File... files) {
        snapshotter.snapshotFiles(Stream.<File> of(files), configuration).collect(Collectors.toList())
    }
}
