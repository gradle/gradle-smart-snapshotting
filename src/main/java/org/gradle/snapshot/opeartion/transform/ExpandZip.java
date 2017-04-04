package org.gradle.snapshot.opeartion.transform;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.FileType;
import org.gradle.snapshot.SnapshottableFile;
import org.gradle.snapshot.Snapshotter;
import org.gradle.snapshot.configuration.ContextElement;
import org.gradle.snapshot.configuration.SingleFileSnapshotOperation;
import org.gradle.snapshot.configuration.SnapshotterContext;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.gradle.snapshot.FileSnapshot.FILE_SNAPSHOT_COMPARATOR;

public class ExpandZip implements SingleFileSnapshotOperation {
    @Override
    public FileSnapshot snapshotSingleFile(SnapshottableFile file, SnapshotterContext context, Snapshotter snapshotter) {
        Stream<FileSnapshot> expandedSnapshots = snapshotter.snapshot(
                expand(file),
                context.addContextElement(new ContextElement(this.getClass()))
        );
        return collect(expandedSnapshots, file);
    }

    private Stream<SnapshottableFile> expand(SnapshottableFile file) {
        ZipInputStream zipInputStream = new ZipInputStream(file.open());

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new ZipIterator(zipInputStream), Spliterator.ORDERED),
                false
        ).onClose(() -> {
            try {
                zipInputStream.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private FileSnapshot collect(Stream<FileSnapshot> snapshots, SnapshottableFile file) {
        try (Stream<FileSnapshot> closableSnapshots = snapshots) {
            return closableSnapshots.sorted(FILE_SNAPSHOT_COMPARATOR).collect(collector(file));
        }
    }

    private Collector<FileSnapshot, ?, FileSnapshot> collector(SnapshottableFile file) {
        return Collectors.collectingAndThen(Collectors.toList(), fileSnapshots -> {
                    Hasher hasher = Hashing.md5().newHasher();
                    fileSnapshots.forEach(fileSnapshot -> hasher.putBytes(fileSnapshot.getHash().asBytes()));
                    return new FileSnapshot(file.getPath(), hasher.hash());
                }
        );
    }

    private static class ZipIterator implements Iterator<SnapshottableFile> {
        private final ZipInputStream inputStream;
        private ZipEntry current;

        private ZipIterator(ZipInputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public boolean hasNext() {
            if (current == null) {
                readNextEntry();
            }
            return current != null;
        }

        private void readNextEntry() {
            try {
                current = inputStream.getNextEntry();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public SnapshottableFile next() {
            ZipSnapshottableFile snapshottableFile = new ZipSnapshottableFile(this.current, inputStream);
            current = null;
            return snapshottableFile;
        }

        private static class ZipSnapshottableFile implements SnapshottableFile {
            private final ZipEntry current;
            private final ZipInputStream inputStream;
            private final FileType fileType;

            private ZipSnapshottableFile(ZipEntry current, ZipInputStream inputStream) {
                this.current = current;
                this.inputStream = inputStream;
                this.fileType = current.isDirectory() ? FileType.DIRECTORY : FileType.REGULAR;
            }

            @Override
            public InputStream open() {
                return new NoCloseInputStream(inputStream);
            }

            @Override
            public String getPath() {
                return current.getName();
            }

            @Override
            public FileType getType() {
                return fileType;
            }
        }
    }

    private static class NoCloseInputStream extends FilterInputStream {
        private NoCloseInputStream(InputStream delegate) {
            super(delegate);
        }

        @Override
        public void close() throws IOException {
        }
    }
}
