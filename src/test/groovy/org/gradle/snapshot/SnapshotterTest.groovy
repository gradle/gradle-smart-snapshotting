package org.gradle.snapshot

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import com.google.common.hash.HashCode
import org.gradle.snapshot.contexts.AbstractContext
import org.gradle.snapshot.contexts.Context
import org.gradle.snapshot.contexts.Result
import org.gradle.snapshot.files.Directoryish
import org.gradle.snapshot.files.Fileish
import org.gradle.snapshot.files.FileishWithContents
import org.gradle.snapshot.files.MissingPhysicalFile
import org.gradle.snapshot.files.PhysicalDirectory
import org.gradle.snapshot.files.PhysicalSnapshot
import org.gradle.snapshot.operations.Operation
import org.gradle.snapshot.operations.ProcessDirectory
import org.gradle.snapshot.operations.ProcessZip
import org.gradle.snapshot.operations.SetContext
import org.gradle.snapshot.rules.ContentRule
import org.gradle.snapshot.rules.PhysicalDirectoryRule
import org.gradle.snapshot.rules.Rule
import org.gradle.snapshot.util.TestFile
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SnapshotterTest extends Specification {
    // Context for runtime classpaths
    static class RuntimeClasspathContext extends AbstractContext {}

    // Context for runtime classpath entries (JAR files and directories)
    static class RuntimeClasspathEntryContext extends AbstractContext {
        @Override
        protected HashCode fold(Collection<Map.Entry<String, Result>> results, Collection<PhysicalSnapshot> physicalSnapshots) {
            // Make sure classpath entries have their elements sorted before combining the hashes
            List<Map.Entry<String, Result>> sortedResults = Lists.newArrayList(results)
            sortedResults.sort { a, b -> a.key <=> b.key }
            super.fold(sortedResults, physicalSnapshots)
        }
    }

    // No-fluff context for regular property snapshotting
    static class DefaultContext extends AbstractContext {}

    // A list of WAR files (with potentially a single element)
    // This is kind of a workaround, as in most cases a property will only have a single WAR file
    // but when snapshotting we only see FileCollections (even if the property's type if File).
    static class WarList extends AbstractContext {}

    // A WAR file
    static class War extends AbstractContext {}

    private static final List<Rule> BASIC_RULES = ImmutableList.<Rule> builder()
        // Hash files in any context
        .add(new Rule(Fileish.class, Context.class, null) {
            @Override
            void process(Fileish file, Context context, List<Operation> operations) throws IOException {
                if (file instanceof FileishWithContents) {
                    context.recordSnapshot(file, file.getContentHash())
                } else if (file instanceof Directoryish) {
                    context.recordSnapshot(file, Directoryish.HASH)
                } else if (file instanceof MissingPhysicalFile) {
                    context.recordSnapshot(file, MissingPhysicalFile.HASH)
                } else {
                    throw new IllegalStateException("Unknown file type: $file (${file.getClass().name})")
                }
            }
        })
        .build()

    private static final List<Rule> RUNTIME_CLASSPATH_RULES = ImmutableList.<Rule> builder()
        // Treat JAR files as classpath entries inside the classpath
        .add(new ContentRule(RuntimeClasspathContext, Pattern.compile(".*\\.jar")) {
            @Override
            protected void processContents(FileishWithContents file, Context context, List<Operation> operations) throws IOException {
                def subContext = context.recordSubContext(file, RuntimeClasspathEntryContext)
                operations.add(new ProcessZip(file, subContext))
            }
        })
        // Treat directories as classpath entries inside the classpath
        .add(new PhysicalDirectoryRule(RuntimeClasspathContext, null) {
            @Override
            protected void processEntries(PhysicalDirectory directory, Context context, List<Operation> operations) throws IOException {
                def subContext = context.recordSubContext(directory, RuntimeClasspathEntryContext)
                operations.add(new ProcessDirectory(directory, subContext))
            }
        })
        // Ignore empty directories inside classpath entries
        .add(new Rule(Directoryish, RuntimeClasspathEntryContext, null) {
            @Override
            void process(Fileish file, Context context, List operations) throws IOException {
            }
        })
        .addAll(BASIC_RULES)
        .build()

    private static final List<Rule> WAR_FILE_RULES = ImmutableList.<Rule> builder()
        // Handle WAR files as WAR files
        .add(new ContentRule(WarList, Pattern.compile(".*\\.war")) {
            @Override
            protected void processContents(FileishWithContents file, Context context, List<Operation> operations) throws IOException {
                def subContext = context.recordSubContext(file, War)
                operations.add(new ProcessZip(file, subContext))
            }
        })
        // Handle directories as exploded WAR files
        .add(new PhysicalDirectoryRule(WarList, null) {
            @Override
            protected void processEntries(PhysicalDirectory directory, Context context, List<Operation> operations) throws IOException {
                def subContext = context.recordSubContext(directory, War)
                operations.add(new ProcessDirectory(directory, subContext))
            }
        })
        // Handle WEB-INF/lib as a runtime classpath
        .add(new Rule(Directoryish, War, Pattern.compile("WEB-INF/lib")) {
            @Override
            void process(Fileish file, Context context, List<Operation> operations) throws IOException {
                def subContext = context.recordSubContext(file, RuntimeClasspathContext)
                operations.add(new SetContext(subContext))
            }
        })
        // Ignore empty directories in WAR files
        .add(new Rule(Directoryish, War, null) {
            @Override
            void process(Fileish file, Context context, List operations) throws IOException {
                // Ignore empty directories inside WAR files
            }
        })
        // Handle runtime classpaths as usual
        .addAll(RUNTIME_CLASSPATH_RULES)
        .build()

    @org.junit.Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    Snapshotter snapshotter = new Snapshotter()

    def "snapshots simple files"() {
        def inputs = [
            file('firstFile.txt').setText("Some text"),
            file('secondFile.txt').setText("Second File"),
            file('missingFile.txt'),
            file('subdir').createDir()
                .file('someOtherFile.log').setText("File in subdir"),
            file('emptydir').createDir(),
        ]

        when:
        def (hash, events, physicalSnapshots) = snapshot(inputs, DefaultContext, BASIC_RULES)
        then:
        hash == "482a3974d523dfaa582f53020835be4b"
        events == [
            "Snapshot taken: firstFile.txt - 9db5682a4d778ca2cb79580bdb67083f",
            "Snapshot taken: secondFile.txt - 82e72efeddfca85ddb625e88af3fe973",
            "Snapshot taken: missingFile.txt - $MissingPhysicalFile.HASH",
            "Snapshot taken: someOtherFile.log - a9cca315f4b8650dccfa3d93284998ef",
            "Snapshot taken: emptydir - $Directoryish.HASH",
            "Folded: DefaultContext - 482a3974d523dfaa582f53020835be4b",
        ]
        physicalSnapshots == [
            "firstFile.txt: 9db5682a4d778ca2cb79580bdb67083f",
            "secondFile.txt: 82e72efeddfca85ddb625e88af3fe973",
            "missingFile.txt: $MissingPhysicalFile.HASH",
            "someOtherFile.log: a9cca315f4b8650dccfa3d93284998ef",
            "emptydir: $Directoryish.HASH",
        ]
    }

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

        when:
        def (hash, events, physicalSnapshots) = snapshot([zipFile, classes], RuntimeClasspathContext, RUNTIME_CLASSPATH_RULES)
        then:
        hash == "a6fb5fc3061570f426ef599fa9b53a73"
        events == [
                "Snapshot taken: library.jar!firstFile.txt - 9db5682a4d778ca2cb79580bdb67083f",
                "Snapshot taken: library.jar!secondFile.txt - 82e72efeddfca85ddb625e88af3fe973",
                "Snapshot taken: library.jar!subdir/someOtherFile.log - a9cca315f4b8650dccfa3d93284998ef",
                "Snapshot taken: classes!fourthFile.txt - 6c99cb370b82c9c527320b35524213e6",
                "Snapshot taken: classes!subdir/build.log - a9cca315f4b8650dccfa3d93284998ef",
                "Snapshot taken: classes!thirdFile.txt - 3f1d3e7fb9620156f8e911fb90d89c42",
                "Folded: RuntimeClasspathContext - a6fb5fc3061570f426ef599fa9b53a73",
        ]
        physicalSnapshots == [
            "library.jar: 429be5439dc0cf3eacb9a48563f00a52",
            "fourthFile.txt: 6c99cb370b82c9c527320b35524213e6",
            "subdir/build.log: a9cca315f4b8650dccfa3d93284998ef",
            "thirdFile.txt: 3f1d3e7fb9620156f8e911fb90d89c42",
            "classes: $Directoryish.HASH",
        ]
    }

    def "can ignore file in runtime classpath"() {
        def zipFile = file('zipContents').create {
            file('firstFile.txt').text = "Some text"
            file('secondFile.txt').text = "Second File"
            subdir {
                file('someOtherFile.log').text = "File in subdir"
            }
        }.createZip(file('library.jar'))

        def rules = ImmutableList.builder()
            // Ignore *.log files inside classpath entries
            .add(new ContentRule(RuntimeClasspathEntryContext, Pattern.compile(".*\\.log")) {
                @Override
                void processContents(FileishWithContents file, Context context, List<Operation> operations) throws IOException {
                    // Do nothing with the file
                }
            })
            .addAll(RUNTIME_CLASSPATH_RULES)
            .build()

        when:
        def (hash, events, physicalSnapshots) = snapshot([zipFile], RuntimeClasspathContext, rules)
        then:
        hash == "1e985e6e85f4cc31ea24b8abd17e42c5"
        events == [
                "Snapshot taken: library.jar!firstFile.txt - 9db5682a4d778ca2cb79580bdb67083f",
                "Snapshot taken: library.jar!secondFile.txt - 82e72efeddfca85ddb625e88af3fe973",
                "Folded: RuntimeClasspathContext - 1e985e6e85f4cc31ea24b8abd17e42c5",
        ]
        physicalSnapshots == [
                "library.jar: dbd9b70c18768d3199c41efef40c73c0",
        ]
    }

    def "recognizes runtime classpath inside war file"() {
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

        // Create WAR file like this so we can break up the runtime classpath in WEB-INF/lib
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

        when:
        def (hash, events, physicalSnapshots) = snapshot([warFile], WarList, WAR_FILE_RULES)
        then:
        hash == "61091b4979095cb64ef7e4c5bede55c2"
        events == [
                "Snapshot taken: web-app.war!WEB-INF/web.xml - 672d3ef8a00bcece517a3fed0f06804b",
                "Snapshot taken: web-app.war!WEB-INF/lib!WEB-INF/lib/guava.jar!com/google/common/collection/Lists.class - 691d1860ec58dd973e803e209697d065",
                "Snapshot taken: web-app.war!WEB-INF/lib!WEB-INF/lib/guava.jar!com/google/common/collection/Sets.class - 86f5baf708c6c250204451eb89736947",
                "Snapshot taken: web-app.war!WEB-INF/lib!WEB-INF/lib/guava.jar!version.properties - 9a0de96b30c230abc8d5263b4c9e22a4",
                "Snapshot taken: web-app.war!README.md - c47c7c7383225ab55ff591cb59c41e6b",
                "Snapshot taken: web-app.war!WEB-INF/lib!WEB-INF/lib/core.jar!org/gradle/Util.class - 23e8a4b4f7cc1898ef12b4e6e48852bb",
                "Folded: WarList - 61091b4979095cb64ef7e4c5bede55c2",
        ]
        physicalSnapshots == [
            "web-app.war: 7124d242b1000e1c054da52d489a07db"
        ]
    }

    private def snapshot(Collection<? extends File> files, Class<? extends Context> contextType, Iterable<Rule> rules) {
        List<String> events = []
        List<PhysicalSnapshot> physicalSnapshots = []
        def context = new RecordingContextWrapper(null, events, contextType.newInstance())
        def hash = snapshotter.snapshot(files, context, rules).fold(physicalSnapshots)
        return [hash.toString(), events, physicalSnapshots*.toString()]
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
        void recordSnapshot(Fileish file, HashCode hash) {
            report("Snapshot taken", file.path, hash)
            delegate.recordSnapshot(file, hash)
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
        <C extends Context> C recordSubContext(Fileish file, Class<C> type) {
            def subContext = delegate.recordSubContext(file, type)
            def wrapper = wrappers.get(subContext)
            if (wrapper == null) {
                wrapper = new RecordingContextWrapper(getFullPath(file.path), events, subContext)
                wrappers.put(subContext, wrapper)
            }
            return (C) wrapper
        }

        @Override
        HashCode fold(Collection<PhysicalSnapshot> physicalSnapshots) {
            def hashCode = delegate.fold(physicalSnapshots)
            report("Folded", getType().simpleName, hashCode)
            return hashCode
        }

        @Override
        Class<? extends Context> getType() {
            return delegate.getType()
        }
    }

    TestFile file(Object... path) {
        return new TestFile(temporaryFolder.root, path)
    }
}
