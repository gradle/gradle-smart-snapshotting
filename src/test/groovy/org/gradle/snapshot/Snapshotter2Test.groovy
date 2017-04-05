package org.gradle.snapshot

import com.google.common.collect.ImmutableList
import org.gradle.snapshot.util.TestFile
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.regex.Pattern

class Snapshotter2Test extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    Snapshotter2 snapshotter = new Snapshotter2()

    TestFile file(Object... path) {
        return new TestFile(temporaryFolder.root, path)
    }

    ImmutableList<Snapshotter2.Rule> runtimeClasspathRules = ImmutableList.<Snapshotter2.Rule> builder()
        .add(new Snapshotter2.AbstractRule<Snapshotter2.FileishWithContents, Snapshotter2.RuntimeClasspathContext>(Snapshotter2.FileishWithContents.class, Snapshotter2.RuntimeClasspathContext.class, Pattern.compile(".*\\.jar")) {
            @Override
            Snapshotter2.Operation processInternal(Snapshotter2.FileishWithContents file, Snapshotter2.RuntimeClasspathContext context) throws IOException {
                return new Snapshotter2.Operation(new Snapshotter2.ZipEnumerator(file), context.subContext(file, Snapshotter2.RuntimeClasspathEntryContext.class));
            }
        })
        .add(new Snapshotter2.DefaultSnapshotRule(Snapshotter2.Context.class, null))
        .build();


    def "can snapshot runtime classpath with JARs"() {
        // This is from the `@Classpath` annotation on the property
        def context = snapshotter.snapshot(files, Snapshotter2.RuntimeClasspathContext, runtimeClasspathRules)
    }

    def "snapshots runtime classpath files"() {
        println temporaryFolder.root.absolutePath
        def zipFile = file('zipContents').create {
            file('firstFile.txt').text = "Some text"
            file('secondFile.txt').text = "Second File"
            subdir {
                file('someOtherFile.log').text = "File in subdir"
            }
        }.createZip(file('library.jar'))

        when:
        def context = snapshotter.snapshot([zipFile], Snapshotter2.RuntimeClasspathContext, runtimeClasspathRules)

        then:
        context.results.values()*.getHashCode()*.toString() == [
                '857b431eae9061f6a5dea3ceb724b8c1',
        ]
    }
}
