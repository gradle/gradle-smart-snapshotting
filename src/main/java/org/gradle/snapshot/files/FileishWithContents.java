package org.gradle.snapshot.files;

import java.io.IOException;
import java.io.InputStream;

public interface FileishWithContents extends Fileish {
    InputStream open() throws IOException;
}
