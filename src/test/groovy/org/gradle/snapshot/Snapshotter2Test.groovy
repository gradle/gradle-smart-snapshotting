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
        .add(new Snapshotter2.FileRule(Snapshotter2.RuntimeClasspathContext, Pattern.compile(".*\\.jar")) {
            @Override
            protected void processInternal(Snapshotter2.FileishWithContents file, Snapshotter2.Context context, List<Snapshotter2.Operation> dependencies) throws IOException {
                def subContext = context.subContext(file, Snapshotter2.RuntimeClasspathEntryContext)
                dependencies.add(new Snapshotter2.ProcessZip(file, subContext))
            }
        })
        .add(new Snapshotter2.Rule(Snapshotter2.Directoryish, Snapshotter2.RuntimeClasspathEntryContext, null) {
            @Override
            void process(Snapshotter2.Fileish file, Snapshotter2.Context context, List dependencies) throws IOException {
                // Ignore empty directories inside classpath entries
                println "Ignored empty dir $file.path"
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
                "Snapshot taken: library.jar!subdir/someOtherFile.log - a9cca315f4b8650dccfa3d93284998ef",
                "Folded: * - 7b367292a129829a58cca166652059d3",
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
                new Snapshotter2.FileRule(Snapshotter2.RuntimeClasspathEntryContext, Pattern.compile(".*\\.log")) {
                    @Override
                    void processInternal(Snapshotter2.FileishWithContents file, Snapshotter2.Context context, List<Snapshotter2.Operation> dependencies) throws IOException {
                        // Do nothing with the file
                        println "Ignoring $file.path"
                    }
                }
            )
            .addAll(runtimeClasspathRules)
            .build()


        expect:
        snapshot([zipFile], Snapshotter2.RuntimeClasspathContext, rules) == [
                "Snapshot taken: library.jar!firstFile.txt - 9db5682a4d778ca2cb79580bdb67083f",
                "Snapshot taken: library.jar!secondFile.txt - 82e72efeddfca85ddb625e88af3fe973",
                "Folded: * - 1e985e6e85f4cc31ea24b8abd17e42c5",
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
