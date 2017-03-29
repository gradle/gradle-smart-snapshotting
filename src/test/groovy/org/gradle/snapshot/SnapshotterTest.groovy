package org.gradle.snapshot

import com.google.common.collect.ImmutableList
import org.gradle.snapshot.hashing.FileHasher
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.stream.Collectors

class SnapshotterTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    Snapshotter snapshotter = new Snapshotter(new FileHasher())

    def "snapshots physical files"() {
        def firstFile = temporaryFolder.newFile('someFile.txt')
        firstFile.text = 'Contents of first file'
        def secondFile = temporaryFolder.newFile('someFile.log')
        secondFile.text = 'Different contents'
        def inputFiles = ImmutableList.of(
                firstFile,
                secondFile,
                temporaryFolder.newFile('empty-file.txt'),
                temporaryFolder.newFolder('someDirectory'),
                new File(temporaryFolder.root, 'does-not-exist.txt')
        )
        expect:
        List<FileSnapshot> result = snapshotter.snapshot(inputFiles.stream()).collect(Collectors.toList())
        result*.hash*.toString() == [
                'b89ea7bbde86a0163569055ff69a7fa6',
                '53862d83a9c70fcd3e0365bbc09c4629',
                'd41d8cd98f00b204e9800998ecf8427e',
                FileHasher.DIRECTORY_HASH.toString(),
                FileHasher.MISSING_FILE_HASH.toString()]
    }
}
