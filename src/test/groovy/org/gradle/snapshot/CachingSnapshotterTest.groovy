package org.gradle.snapshot

import com.google.common.hash.HashCode
import com.google.common.hash.Hashing
import org.gradle.snapshot.configuration.SnapshotterConfiguration
import org.gradle.snapshot.operation.snapshot.CachingSnapshotOperation
import org.gradle.snapshot.operation.transform.ExpandZip
import org.gradle.snapshot.operation.transform.Filter

import static org.gradle.snapshot.configuration.DefaultOperationBinding.binding
import static org.gradle.snapshot.configuration.ZipFileMatcher.IS_ZIP_FILE

class CachingSnapshotterTest extends AbstractSnapshotterTest {

    def "cache per file"() {
        Map<HashCode, HashCode> cache = new HashMap<>()

        configuration = configuration.withSnapshotOperation(
                binding(new CachingSnapshotOperation(new ExpandZip(), cache, new SnapshotterConfiguration()),
                        IS_ZIP_FILE
                )
        )

        def zipContents = file('zipContents').create {
            file('firstFile.txt').text = "Some text"
            file('secondFile.txt').text = "Second File"
            subdir {
                file('someOtherFile.log').text = "File in subdir"
            }
        }
        def zipFile = zipContents.createZip(file('input.zip'))

        when:
        def result = snapshotFiles(zipFile)

        then:
        cache.size() == 1
        def singleCacheEntry = cache.entrySet().iterator().next()
        def hashOfZipFile = singleCacheEntry.getValue()
        assert singleCacheEntry.getKey() != hashOfZipFile
        result*.hash == [hashOfZipFile]

        def newHashKey = Hashing.md5().hashUnencodedChars("Different hash key")
        when:
        cache.put(singleCacheEntry.getKey(), newHashKey)

        result = snapshotFiles(zipFile)

        then:
        cache.size() == 1
        result*.hash == [newHashKey]

        when:
        cache.put(singleCacheEntry.getKey(), hashOfZipFile)
        zipFile.delete()
        zipContents.file('firstFile.txt').makeOlder()
        zipContents.createZip(zipFile)
        snapshotFiles(zipFile)

        then:
        cache.size() == 2
        cache.values() as Set == [hashOfZipFile] as Set
    }

    def "cache zip file based on expanded zip file"() {
        Map<HashCode, HashCode> cache = new HashMap<>()

        configuration = configuration.withSnapshotOperation(
                binding(new CachingSnapshotOperation(new ExpandZip(), cache,
                        new SnapshotterConfiguration().withSnapshotOperation(binding(new ExpandZip(), IS_ZIP_FILE))
                ), IS_ZIP_FILE)
        ).withTransform(
                binding(new Filter(), { file, context -> file.path.endsWith('.properties') })
        )
        def zipContents = file('zipContents').create {
            file('firstFile.txt').text = "Some text"
            file('secondFile.txt').text = "Second File"
            file('some.properties').text = "some=xxx"
            subdir {
                file('someOtherFile.log').text = "File in subdir"
            }
        }
        def zipFile = zipContents.createZip(file('input.zip'))

        when:
        snapshotFiles(zipFile)

        then:
        cache.size() == 1
        def firstHash = cache.values().iterator().next()

        when:
        file(zipContents, 'some.properties').text = 'some=zzz'
        zipFile.delete()
        zipContents.createZip(zipFile)
        snapshotFiles(zipFile)

        then:
        cache.size() == 1
        cache.values().iterator().next() == firstHash
    }
}
