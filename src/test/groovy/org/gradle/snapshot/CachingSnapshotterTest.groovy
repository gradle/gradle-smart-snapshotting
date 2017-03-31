package org.gradle.snapshot

import com.google.common.hash.HashCode
import com.google.common.hash.Hashing
import org.gradle.snapshot.configuration.CachingSnapshotOperation
import org.gradle.snapshot.configuration.DefaultSnapshotterModifier
import org.gradle.snapshot.configuration.ExpandZip
import org.gradle.snapshot.configuration.SnapshotterContext

class CachingSnapshotterTest extends AbstractSnapshotterTest {

    def "Can cache per file"() {
        Map<HashCode, HashCode> cache = new HashMap<>()

        context = context.withSnapshotOperation(
                DefaultSnapshotterModifier.modifier(
                        { file, ctx -> file.path.endsWith('.zip') && ctx.isEmpty() },
                        new CachingSnapshotOperation(new ExpandZip(), cache, new SnapshotterContext()))
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
        assert singleCacheEntry.getKey() != singleCacheEntry.getValue()
        result*.hash == [singleCacheEntry.getValue()]

        def newHashKey = Hashing.md5().hashUnencodedChars("Different hash key")
        when:
        cache.put(singleCacheEntry.getKey(), newHashKey)

        result = snapshotFiles(zipFile)

        then:
        result*.hash == [newHashKey]
    }
}
