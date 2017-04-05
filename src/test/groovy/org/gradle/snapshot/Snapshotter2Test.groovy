package org.gradle.snapshot

import com.google.common.collect.ImmutableList
import com.google.common.hash.HashCode
import org.gradle.snapshot.Snapshotter2.Context
import org.gradle.snapshot.Snapshotter2.DirectoryRule
import org.gradle.snapshot.Snapshotter2.Directoryish
import org.gradle.snapshot.Snapshotter2.FileRule
import org.gradle.snapshot.Snapshotter2.Fileish
import org.gradle.snapshot.Snapshotter2.FileishWithContents
import org.gradle.snapshot.Snapshotter2.Operation
import org.gradle.snapshot.Snapshotter2.PhysicalDirectory
import org.gradle.snapshot.Snapshotter2.ProcessDirectory
import org.gradle.snapshot.Snapshotter2.ProcessZip
import org.gradle.snapshot.Snapshotter2.Rule
import org.gradle.snapshot.Snapshotter2.RuntimeClasspathContext
import org.gradle.snapshot.Snapshotter2.RuntimeClasspathEntryContext
import org.gradle.snapshot.Snapshotter2.SetContext
import org.gradle.snapshot.Snapshotter2.SnapshotRule
import org.gradle.snapshot.Snapshotter2.War
import org.gradle.snapshot.Snapshotter2.WarList
import org.gradle.snapshot.util.TestFile
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class Snapshotter2Test extends Specification {
    @org.junit.Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    Snapshotter2 snapshotter = new Snapshotter2()

    TestFile file(Object... path) {
        return new TestFile(temporaryFolder.root, path)
    }

    List<Rule> runtimeClasspathRules = ImmutableList.<Rule> builder()
        .add(new FileRule(RuntimeClasspathContext, Pattern.compile(".*\\.jar")) {
            @Override
            protected void processContents(FileishWithContents file, Context context, List<Operation> dependencies) throws IOException {
                def subContext = context.subContext(file, RuntimeClasspathEntryContext)
                dependencies.add(new ProcessZip(file, subContext))
            }
        })
        .add(new DirectoryRule(RuntimeClasspathContext, null) {
            @Override
            protected void processEntries(PhysicalDirectory directory, Context context, List<Operation> dependencies) throws IOException {
                def subContext = context.subContext(directory, RuntimeClasspathEntryContext)
                dependencies.add(new ProcessDirectory(directory, subContext))
            }
        })
        .add(new Rule(Directoryish, RuntimeClasspathEntryContext, null) {
            @Override
            void process(Fileish file, Context context, List dependencies) throws IOException {
                // Ignore empty directories inside classpath entries
            }
        })
        .add(new SnapshotRule(Context.class, null))
        .build()

    List<Rule> warFileRules = ImmutableList.<Rule> builder()
        .add(new FileRule(WarList, Pattern.compile(".*\\.war")) {
            @Override
            protected void processContents(FileishWithContents file, Context context, List<Operation> dependencies) throws IOException {
                def subContext = context.subContext(file, War)
                dependencies.add(new ProcessZip(file, subContext))
            }
        })
        .add(new DirectoryRule(RuntimeClasspathContext, null) {
            @Override
            protected void processEntries(PhysicalDirectory directory, Context context, List<Operation> dependencies) throws IOException {
                def subContext = context.subContext(directory, War)
                dependencies.add(new ProcessDirectory(directory, subContext))
            }
        })
        .add(new Rule(Directoryish, War, Pattern.compile("WEB-INF/lib")) {
            @Override
            void process(Fileish file, Context context, List<Operation> dependencies) throws IOException {
                def subContext = context.subContext(file, RuntimeClasspathContext)
                dependencies.add(new SetContext(subContext))
            }
        })
        .add(new Rule(Directoryish, War, null) {
            @Override
            void process(Fileish file, Context context, List dependencies) throws IOException {
                // Ignore empty directories inside WAR files
            }
        })
        .addAll(runtimeClasspathRules)
        .build()

    def "snapshots runtime classpath files"() {
        def zipFile = file('zipContents').create {
            file('firstFile.txt').text = "Some text"
            file('secondFile.txt').text = "Second File"
            subdir {
                file('someOtherFile.log').text = "File in subdir"
            }
        }.createZip(file('library.jar'))
        def classes = file('classes').create {
            file('thirdFile.txt').text = "Third file"
            file('fourthFile.txt').text = "Fourth file"
            subdir {
                file('build.log').text = "File in subdir"
            }
        }

        expect:
        snapshot([zipFile, classes], RuntimeClasspathContext, runtimeClasspathRules) == [
                "Snapshot taken: library.jar!firstFile.txt - 9db5682a4d778ca2cb79580bdb67083f",
                "Snapshot taken: library.jar!secondFile.txt - 82e72efeddfca85ddb625e88af3fe973",
                "Snapshot taken: library.jar!subdir/someOtherFile.log - a9cca315f4b8650dccfa3d93284998ef",
                "Snapshot taken: classes!fourthFile.txt - 6c99cb370b82c9c527320b35524213e6",
                "Snapshot taken: classes!subdir/build.log - a9cca315f4b8650dccfa3d93284998ef",
                "Snapshot taken: classes!thirdFile.txt - 3f1d3e7fb9620156f8e911fb90d89c42",
                "Folded: RuntimeClasspathContext - a6fb5fc3061570f426ef599fa9b53a73",
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
                new FileRule(RuntimeClasspathEntryContext, Pattern.compile(".*\\.log")) {
                    @Override
                    void processContents(FileishWithContents file, Context context, List<Operation> dependencies) throws IOException {
                        // Do nothing with the file
                        println "Ignoring $file.path"
                    }
                }
            )
            .addAll(runtimeClasspathRules)
            .build()


        expect:
        snapshot([zipFile], RuntimeClasspathContext, rules) == [
                "Snapshot taken: library.jar!firstFile.txt - 9db5682a4d778ca2cb79580bdb67083f",
                "Snapshot taken: library.jar!secondFile.txt - 82e72efeddfca85ddb625e88af3fe973",
                "Folded: RuntimeClasspathContext - 1e985e6e85f4cc31ea24b8abd17e42c5",
        ]
    }

    def "recognizes runtime classpath inside war file"() {
        println temporaryFolder.root.absolutePath
        def guavaJar = file('guavaContents').create {
            "com" {
                "google" {
                    "common" {
                        "collection" {
                            file('Lists.class').text = "Lists"
                            file('Sets.class').text = "Sets"
                        }
                    }
                }
            }
            file('version.properties').text = "version=1.0"
        }.createZip(file('guava.jar'))
        def coreJar = file('coreContents').create {
            "org" {
                "gradle" {
                    file('Util.class').text = "Util"
                }
            }
        }.createZip(file('core.jar'))

        // Create WAR file like this so we can put JARs in WEB-INF/lib non-consecutively
        def warFile = file("web-app.war")
        def warFileOut = new ZipOutputStream(new FileOutputStream(warFile))
        warFileOut.putNextEntry(new ZipEntry("WEB-INF/"))
        warFileOut.putNextEntry(new ZipEntry("WEB-INF/web.xml"))
        warFileOut << "<web/>".bytes
        warFileOut.putNextEntry(new ZipEntry("WEB-INF/lib/"))
        warFileOut.putNextEntry(new ZipEntry("WEB-INF/lib/guava.jar"))
        warFileOut << guavaJar.bytes
        warFileOut.putNextEntry(new ZipEntry("README.md"))
        warFileOut << "README".bytes
        warFileOut.putNextEntry(new ZipEntry("WEB-INF/lib/core.jar"))
        warFileOut << coreJar.bytes
        warFileOut.close()

        expect:
        snapshot([warFile], WarList, warFileRules) == [
                "Snapshot taken: web-app.war!WEB-INF/web.xml - 672d3ef8a00bcece517a3fed0f06804b",
                "Snapshot taken: web-app.war!WEB-INF/lib!WEB-INF/lib/guava.jar!com/google/common/collection/Lists.class - 691d1860ec58dd973e803e209697d065",
                "Snapshot taken: web-app.war!WEB-INF/lib!WEB-INF/lib/guava.jar!com/google/common/collection/Sets.class - 86f5baf708c6c250204451eb89736947",
                "Snapshot taken: web-app.war!WEB-INF/lib!WEB-INF/lib/guava.jar!version.properties - 9a0de96b30c230abc8d5263b4c9e22a4",
                "Snapshot taken: web-app.war!README.md - c47c7c7383225ab55ff591cb59c41e6b",
                "Snapshot taken: web-app.war!WEB-INF/lib!WEB-INF/lib/core.jar!org/gradle/Util.class - 23e8a4b4f7cc1898ef12b4e6e48852bb",
                "Folded: WarList - 61091b4979095cb64ef7e4c5bede55c2",
        ]
    }

    private List<String> snapshot(Collection<File> files, Class<? extends Context> contextType, Iterable<Rule> rules) {
        def events = []
        def context = new RecordingContextWrapper(null, events, contextType.newInstance())
        snapshotter.snapshot(files, context, rules).fold()
        return events
    }

    private static class RecordingContextWrapper implements Context {
        List<String> events
        String path
        Context delegate
        Map<Context, RecordingContextWrapper> wrappers = [:]

        RecordingContextWrapper() {
        }

        RecordingContextWrapper(String path, List<String> events, Context delegate) {
            this.events = events
            this.path = path
            this.delegate = delegate
        }

        @Override
        void snapshot(Fileish file, HashCode hash) {
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
        <C extends Context> C subContext(Fileish file, Class<C> type) {
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
            report("Folded", getType().simpleName, result)
            return result
        }

        @Override
        Class<? extends Context> getType() {
            return delegate.getType()
        }
    }
}
