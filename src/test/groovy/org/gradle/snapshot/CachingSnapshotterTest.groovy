package org.gradle.snapshot

import com.google.common.hash.HashCode
import com.google.common.hash.Hashing
import org.gradle.snapshot.configuration.CachingSnapshotOperation
import org.gradle.snapshot.configuration.ExpandZip
import org.gradle.snapshot.configuration.Filter
import org.gradle.snapshot.configuration.SnapshotOperationBindings

import static org.gradle.snapshot.configuration.DefaultSnapshotOperationBinding.binding
import static org.gradle.snapshot.configuration.ZipFileMatcher.IS_ZIP_FILE

class CachingSnapshotterTest extends AbstractSnapshotterTest {

    def "cache per file"() {
        Map<HashCode, HashCode> cache = new HashMap<>()

        bindings = bindings.withBinding(
                binding(new CachingSnapshotOperation(new ExpandZip(), cache, new SnapshotOperationBindings()),
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

        bindings = bindings.withBinding(
                binding(new CachingSnapshotOperation(new ExpandZip(), cache,
                        new SnapshotOperationBindings().withBinding(binding(new ExpandZip(), IS_ZIP_FILE))
                ), IS_ZIP_FILE)
        ).withBinding(
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
