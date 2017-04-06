package org.gradle.snapshot.files;

import org.apache.commons.io.input.CloseShieldInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

public class ZipEntryFile extends AbstractFileish implements FileishWithContents {
    private final ZipInputStream inputStream;

    public ZipEntryFile(String path, ZipInputStream inputStream) {
        super(path);
        this.inputStream = inputStream;
    }

    @Override
    public InputStream open() throws IOException {
        return new CloseShieldInputStream(inputStream);
    }
}
