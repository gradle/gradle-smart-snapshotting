package org.gradle.snapshot.configuration;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import ix.Ix;
import org.gradle.snapshot.FileSnapshot;
import org.gradle.snapshot.FileType;
import org.gradle.snapshot.SnapshottableFile;
import org.gradle.snapshot.Snapshotter;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.gradle.snapshot.FileSnapshot.FILE_SNAPSHOT_COMPARATOR;

public class ExpandZip implements SingleFileSnapshotOperation {
    @Override
    public FileSnapshot snapshotSingleFile(SnapshottableFile file, SnapshotterContext context, Snapshotter snapshotter) {
        Ix<FileSnapshot> expandedSnapshots = snapshotter.snapshot(
                expand(file),
                context.addContextElement(new ContextElement(this.getClass()))
        );
        return collect(expandedSnapshots, file);
    }

    private Ix<SnapshottableFile> expand(SnapshottableFile file) {
        return Ix.generate(
                () -> new ZipInputStream(file.open()),
                (state, emitter) -> {
                    try {
                        ZipEntry nextEntry = state.getNextEntry();
                        if (nextEntry != null) {
                            emitter.onNext(new ZipSnapshottableFile(nextEntry, state));
                        } else {
                            emitter.onComplete();
                        }
                        return state;
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }, state -> {
                    try {
                        state.close();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    private FileSnapshot collect(Ix<FileSnapshot> snapshots, SnapshottableFile file) {
        return combineHashes(file, snapshots.orderBy(FILE_SNAPSHOT_COMPARATOR).toList());
    }

    private FileSnapshot combineHashes(SnapshottableFile root, List<FileSnapshot> fileSnapshots) {
        Hasher hasher = Hashing.md5().newHasher();
        fileSnapshots.forEach(fileSnapshot -> hasher.putBytes(fileSnapshot.getHash().asBytes()));
        return new FileSnapshot(root.getPath(), hasher.hash());
    }

    private static class NoCloseInputStream extends FilterInputStream {
        private NoCloseInputStream(InputStream delegate) {
            super(delegate);
        }

        @Override
        public void close() throws IOException {
        }
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
