package org.gradle.snapshot

import com.google.common.collect.ImmutableList
import org.gradle.snapshot.configuration.ExpandZip
import org.gradle.snapshot.configuration.Filter
import org.gradle.snapshot.configuration.SnapshotterConfiguration
import org.gradle.snapshot.hashing.FileHasher
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Files
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static java.util.stream.Collectors.toList
import static org.gradle.snapshot.configuration.DefaultSnapshotterModifier.modifier
import static org.gradle.snapshot.configuration.ZipFileMatcher.IS_ZIP_FILE

class SnapshotterTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    Snapshotter snapshotter = new Snapshotter(new FileHasher(), new SnapshotterConfiguration())

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
        snapshotter = new Snapshotter(new FileHasher(), new SnapshotterConfiguration(modifier(IS_ZIP_FILE, new ExpandZip())))

        def zipContents = temporaryFolder.newFolder('zipContents')
        def firstFile = new File(zipContents, "firstFile.txt")
        firstFile.createNewFile()
        firstFile.text = "Some text"
        def secondFile = new File(zipContents, "secondFile.txt")
        secondFile.createNewFile()
        secondFile.text = "second file"

        def fileNotInZip = temporaryFolder.newFile("unrelatedFile.txt")
        fileNotInZip.text = "Contents of first file"

        def zipFile = temporaryFolder.newFile("input.zip")
        zip(zipFile, zipContents)

        when:
        List<FileSnapshot> snapshots = snapshotter.snapshotFiles([zipFile, fileNotInZip].stream()).collect(Collectors.<FileSnapshot> toList())

        then:
        snapshots*.hash*.toString() == [
                'adfb1f084e157fd1b1814912b44ca8ef',
                'b89ea7bbde86a0163569055ff69a7fa6'
        ]
    }

    def "can look into zip files in zip files"() {
        snapshotter = new Snapshotter(new FileHasher(), new SnapshotterConfiguration(modifier(IS_ZIP_FILE, new ExpandZip())))

        def zipInZipContents = temporaryFolder.newFolder('zipInZipContents')
        def firstFileInZip = new File(zipInZipContents, "firstFileInZip.txt")
        firstFileInZip.createNewFile()
        firstFileInZip.text = "Some text in zip"
        def secondFileInZip = new File(zipInZipContents, "secondFileInZip.txt")
        secondFileInZip.createNewFile()
        secondFileInZip.text = "second file in zip"


        def zipContents = temporaryFolder.newFolder('zipContents')
        def firstFile = new File(zipContents, "firstFile.txt")
        firstFile.createNewFile()
        firstFile.text = "Some text"
        def secondFile = new File(zipContents, "secondFile.txt")
        secondFile.createNewFile()
        secondFile.text = "second file"
        zip(new File(zipContents, "zipInZip.zip"), zipInZipContents)

        def fileNotInZip = temporaryFolder.newFile("unrelatedFile.txt")
        fileNotInZip.text = "Contents of first file"

        def zipFile = temporaryFolder.newFile("input.zip")
        zip(zipFile, zipContents)

        when:
        List<FileSnapshot> snapshots = snapshotter.snapshotFiles([zipFile, fileNotInZip].stream()).collect(Collectors.<FileSnapshot>toList())

        then:
        snapshots*.hash*.toString() == [
                '60504d6d431f8d21a01d03df9875dc48',
                'b89ea7bbde86a0163569055ff69a7fa6'
        ]
    }

    def "can ignore files"() {
        snapshotter = new Snapshotter(new FileHasher(), new SnapshotterConfiguration(
                null,
                modifier({ it -> it.path.contains('ignored')}, new Filter())))

        def firstFile = temporaryFolder.newFile("my-name.txt")
        firstFile.text = "I am snapshotted"
        def ignoredFile = temporaryFolder.newFile("ignored.txt")
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

        def zipContents = temporaryFolder.newFolder('zipContents')
        def firstFileInZip = new File(zipContents, "firstFileInZip.txt")
        firstFileInZip.createNewFile()
        firstFileInZip.text = "Some text in zip"
        def ignoredFile = new File(zipContents, "ignoredFile.txt")
        ignoredFile.createNewFile()
        ignoredFile.text = "Not snapshotted"
        def zipFile = temporaryFolder.newFile("zipFile.zip")
        zip(zipFile, zipContents)

        when:
        List<FileSnapshot> result = snapshotter.snapshotFiles([zipFile].stream()).collect(toList())

        then:
        result.size() == 1
        def hashOfZip = result.first().hash

        when:
        ignoredFile << "I am also ignored."
        zipFile.delete()
        zip(zipFile, zipContents)
        result = snapshotter.snapshotFiles([zipFile].stream()).collect(toList())

        then:
        result.size() == 1
        result.first().hash == hashOfZip


    }

    private static void zip(File zipFile, File inputDir) {
        new ZipOutputStream(new FileOutputStream(zipFile)).withStream { zipOutputStream ->
            inputDir.eachFile() { file ->
                zipOutputStream.putNextEntry(zipEntry(file))
                Files.copy(file.toPath(), zipOutputStream)
                zipOutputStream.closeEntry()
            }
        }
    }

    private static ZipEntry zipEntry(File file) {
        def entry = new ZipEntry(file.getName())
        entry.setTime(file.lastModified())
        return entry
    }
}
