package org.gradle.snapshot.operations;

import org.gradle.snapshot.SnapshotterState;
import org.gradle.snapshot.contexts.Context;
import org.gradle.snapshot.files.FileishWithContents;
import org.gradle.snapshot.files.ZipEntryDirectory;
import org.gradle.snapshot.files.ZipEntryFile;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ProcessZip extends Operation {
    private final FileishWithContents file;
    private ZipInputStream input;

    public ProcessZip(FileishWithContents file, Context context) {
        super(context);
        this.file = file;
    }

    @Override
    public boolean execute(SnapshotterState state, List<Operation> dependencies) throws IOException {
        if (input == null) {
            input = new ZipInputStream(file.open());
        }
        ZipEntry entry = input.getNextEntry();
        if (entry == null) {
            return true;
        }

        // Match against directories
        String path = entry.getName();
        int index = -1;
        while ((index = path.indexOf('/', index + 1)) != -1) {
            dependencies.add(new ApplyTo(new ZipEntryDirectory(path.substring(0, index), file)));
        }

        // Match against the file
        if (!entry.isDirectory()) {
            dependencies.add(new ApplyTo(new ZipEntryFile(path, file, input)));
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        if (input != null) {
            ZipInputStream inputToClose = this.input;
            this.input = null;
            inputToClose.close();
        }
    }
}
