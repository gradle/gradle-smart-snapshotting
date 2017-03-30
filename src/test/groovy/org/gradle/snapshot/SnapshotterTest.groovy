package org.gradle.snapshot

import com.google.common.collect.ImmutableList
import org.gradle.snapshot.configuration.ExpandZip
import org.gradle.snapshot.configuration.Filter
import org.gradle.snapshot.configuration.SnapshotterConfiguration
import org.gradle.snapshot.hashing.FileHasher
import org.gradle.snapshot.util.TestFile
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static java.util.stream.Collectors.toList
import static org.gradle.snapshot.configuration.DefaultSnapshotterModifier.modifier
import static org.gradle.snapshot.configuration.ZipFileMatcher.IS_ZIP_FILE

class SnapshotterTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    Snapshotter snapshotter = new Snapshotter(new FileHasher(), new SnapshotterConfiguration())

    TestFile file(Object... path) {
        return new TestFile(temporaryFolder.root, path)
    }

    def "snapshots physical files"() {
        TestFile firstFile = file('someFile.txt')
        firstFile.text = 'Contents of first file'
        TestFile secondFile = file('someFile.log')
        secondFile.text = 'Different contents'
        def inputFiles = ImmutableList.of(
                firstFile,
                secondFile,
                file('empty-file.txt').createFile(),
                file('someDirectory').createDir(),
                file(temporaryFolder.root, 'does-not-exist.txt')
        )

        when:
        List<FileSnapshot> result = snapshotter.snapshotFiles(inputFiles.stream()).collect(toList())

        then:
        result*.hash*.toString() == [
                'b89ea7bbde86a0163569055ff69a7fa6',
                '53862d83a9c70fcd3e0365bbc09c4629',
                'd41d8cd98f00b204e9800998ecf8427e',
                FileHasher.DIRECTORY_HASH.toString(),
                FileHasher.MISSING_FILE_HASH.toString()]
    }

    def "can look into zip files"() {
        snapshotter = new Snapshotter(new FileHasher(),
                new SnapshotterConfiguration(modifier(IS_ZIP_FILE, new ExpandZip())))

        def zipFile = file('zipContents').create {
            file('firstFile.txt').text = "Some text"
            file('secondFile.txt').text = "Second File"
            subdir {
                file('someOtherFile.log').text = "File in subdir"
            }
        }.createZip(file('input.zip'))

        def fileNotInZip = file("unrelatedFile.txt")
        fileNotInZip.text = "Contents of first file"

        when:
        List<FileSnapshot> snapshots = snapshotter.snapshotFiles([zipFile, fileNotInZip].stream()).collect(toList())

        then:
        snapshots*.hash*.toString() == [
                'fe69ba43604ef7197e1d4dd3a7f57d2e',
                'b89ea7bbde86a0163569055ff69a7fa6'
        ]
    }

    def "can look into zip files in zip files"() {
        snapshotter = new Snapshotter(new FileHasher(), new SnapshotterConfiguration(modifier(IS_ZIP_FILE, new ExpandZip())))

        def zipInZipContents = file('zipInZipContents').create {
            file("firstFileInZip.txt").text = "Some text in zip"
            file("secondFileInZip.txt").text = "second file in zip"
        }


        def zipContents = file('zipContents').create {
            file("firstFile.txt").text = "Some text"
            file("secondFile.txt").text = "second file"
            zipInZipContents.createZip(file("zipInZip.zip"))
        }

        def fileNotInZip = file("unrelatedFile.txt")
        fileNotInZip.text = "Contents of first file"

        def zipFile = zipContents.createZip(file('zipFile.zip'))

        when:
        List<FileSnapshot> snapshots = snapshotter.snapshotFiles([zipFile, fileNotInZip].stream()).collect(toList())

        then:
        snapshots*.hash*.toString() == [
                '60504d6d431f8d21a01d03df9875dc48',
                'b89ea7bbde86a0163569055ff69a7fa6'
        ]
    }

    def "can ignore files"() {
        snapshotter = new Snapshotter(new FileHasher(), new SnapshotterConfiguration(
                modifier({ it -> it.path.contains('ignored')}, new Filter())))

        def firstFile = file("my-name.txt")
        firstFile.text = "I am snapshotted"
        def ignoredFile = file("ignored.txt")
        ignoredFile.text = "I am not snapshotted."

        when:
        List<FileSnapshot> result = snapshotter.snapshotFiles([firstFile, ignoredFile].stream()).collect(toList())

        then:
        result.size() == 1

        def hashOfNotIgnoredFile = result.first().hash

        when:
        ignoredFile << "I am also ignored."
        result = snapshotter.snapshotFiles([ignoredFile, firstFile].stream()).collect(toList())

        then:
        result.size() == 1
        result.first().hash == hashOfNotIgnoredFile
    }

    def "can ignore files in zip"() {
        snapshotter = new Snapshotter(new FileHasher(), new SnapshotterConfiguration(
                modifier(IS_ZIP_FILE, new ExpandZip()),
                modifier({ it -> it.path.contains('ignored')}, new Filter())))

        def zipContents = file('zipContents').create {
            file("firstFileInZip.txt").text = "Some text in zip"
        }
        def ignoredFile = file(zipContents, "ignoredFile.txt")
        ignoredFile.text = "Not snapshotted"

        def zipFile = zipContents.createZip(file("notIgnoredZipFile.zip"))

        when:
        List<FileSnapshot> result = snapshotter.snapshotFiles([zipFile].stream()).collect(toList())

        then:
        result.size() == 1
        def hashOfZip = result.first().hash

        when:
        ignoredFile << "I am also ignored."
        zipFile.delete()
        zipContents.createZip(file("notIgnoredZipFile.zip"))

        result = snapshotter.snapshotFiles([zipFile].stream()).collect(toList())

        then:
        result.size() == 1
        result.first().hash == hashOfZip
    }
}
