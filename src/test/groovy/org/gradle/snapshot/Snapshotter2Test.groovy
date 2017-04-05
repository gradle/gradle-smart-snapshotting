package org.gradle.snapshot

import com.google.common.collect.ImmutableList
import com.google.common.hash.HashCode
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

    List<Snapshotter2.Rule> runtimeClasspathRules = ImmutableList.<Snapshotter2.Rule> builder()
        .add(new Snapshotter2.AbstractRule<Snapshotter2.FileishWithContents, Snapshotter2.RuntimeClasspathContext>(Snapshotter2.FileishWithContents.class, Snapshotter2.RuntimeClasspathContext.class, Pattern.compile(".*\\.jar")) {
            @Override
            Snapshotter2.Operation processInternal(Snapshotter2.FileishWithContents file, Snapshotter2.Context context) throws IOException {
                return new Snapshotter2.Operation(new Snapshotter2.ZipEnumerator(file), context.subContext(file, Snapshotter2.RuntimeClasspathEntryContext.class))
            }
        })
        .add(new Snapshotter2.DefaultSnapshotRule(Snapshotter2.Context.class, null))
        .build()

    def "snapshots runtime classpath files"() {
        def zipFile = file('zipContents').create {
            file('firstFile.txt').text = "Some text"
            file('secondFile.txt').text = "Second File"
            subdir {
                file('someOtherFile.log').text = "File in subdir"
            }
        }.createZip(file('library.jar'))

        expect:
        snapshot([zipFile], Snapshotter2.RuntimeClasspathContext, runtimeClasspathRules) == [
                "Snapshot taken: library.jar!firstFile.txt - 9db5682a4d778ca2cb79580bdb67083f",
                "Snapshot taken: library.jar!secondFile.txt - 82e72efeddfca85ddb625e88af3fe973",
                "Snapshot taken: library.jar!subdir/ - d41d8cd98f00b204e9800998ecf8427e",
                "Snapshot taken: library.jar!subdir/someOtherFile.log - a9cca315f4b8650dccfa3d93284998ef",
                "Folded: * - 8b104f76bd5356888630870e0e8fec79",
        ]
    }

    def "can ignore file in runtime classpath"() {
        println temporaryFolder.root.absolutePath
        def zipFile = file('zipContents').create {
            file('firstFile.txt').text = "Some text"
            file('secondFile.txt').text = "Second File"
            subdir {
                file('someOtherFile.log').text = "File in subdir"
            }
        }.createZip(file('library.jar'))

        def rules = ImmutableList.builder()
            .add(
                new Snapshotter2.AbstractRule<Snapshotter2.Fileish, Snapshotter2.RuntimeClasspathEntryContext>(Snapshotter2.Fileish, Snapshotter2.RuntimeClasspathEntryContext, Pattern.compile(".*\\.log")) {
                    @Override
                    Snapshotter2.Operation processInternal(Snapshotter2.Fileish file, Snapshotter2.Context context) throws IOException {
                        // Do nothing with the file
                        println "Ignoring $file.path"
                        return null
                    }
                }
            )
            .addAll(runtimeClasspathRules)
            .build()


        expect:
        snapshot([zipFile], Snapshotter2.RuntimeClasspathContext, rules) == [
                "Snapshot taken: library.jar!firstFile.txt - 9db5682a4d778ca2cb79580bdb67083f",
                "Snapshot taken: library.jar!secondFile.txt - 82e72efeddfca85ddb625e88af3fe973",
                "Snapshot taken: library.jar!subdir/ - d41d8cd98f00b204e9800998ecf8427e",
                "Folded: * - 0660cdb0b05c8bede7e17a8b097cfb74",
        ]
    }

    private List<String> snapshot(Collection<File> files, Class<? extends Snapshotter2.Context> contextType, Iterable<Snapshotter2.Rule> rules) {
        def events = []
        def context = new RecordingContextWrapper(null, events, contextType.newInstance())
        snapshotter.snapshot(files, context, rules).fold()
        return events
    }

    private static class RecordingContextWrapper implements Snapshotter2.Context {
        private final List<String> events
        private final String path
        private final Snapshotter2.Context delegate
        private final Map<Snapshotter2.Context, RecordingContextWrapper> wrappers = [:]

        RecordingContextWrapper(String path, List<String> events, Snapshotter2.Context delegate) {
            this.events = events
            this.path = path
            this.delegate = delegate
        }

        @Override
        void snapshot(Snapshotter2.Fileish file, HashCode hash) {
            report("Snapshot taken", file.path, hash)
            delegate.snapshot(file, hash)
        }

        private void report(String type, String filePath, HashCode hash) {
            def event = "$type: ${getFullPath(filePath)} - $hash"
            events.add(event)
            println event
        }

        private String getFullPath(String filePath) {
            return path ? "$path!$filePath" : filePath
        }

        @Override
        <C extends Snapshotter2.Context> C subContext(Snapshotter2.Fileish file, Class<C> type) {
            def subContext = delegate.subContext(file, type)
            def wrapper = wrappers.get(subContext)
            if (wrapper == null) {
                wrapper = new RecordingContextWrapper(getFullPath(file.path), events, subContext)
                wrappers.put(subContext, wrapper)
            }
            return (C) wrapper
        }

        @Override
        HashCode fold() {
            def result = delegate.fold()
            report("Folded", "*", result)
            return result
        }

        @Override
        Class<? extends Snapshotter2.Context> getType() {
            return delegate.getType()
        }
    }
}
