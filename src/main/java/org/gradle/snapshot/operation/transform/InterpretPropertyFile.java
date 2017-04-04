package org.gradle.snapshot.operation.transform;

import com.google.common.collect.ImmutableSortedMap;
import org.gradle.snapshot.FileType;
import org.gradle.snapshot.SnapshottableFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InterpretPropertyFile implements TransformOperation {
    @Override
    public Stream<SnapshottableFile> transform(SnapshottableFile snapshottableFile) {
        try (InputStream inputStream = snapshottableFile.open()) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return Stream.of(new PropertyFile(snapshottableFile.getPath(), properties));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class PropertyFile implements SnapshottableFile {
        private final String path;
        private final Properties properties;

        private PropertyFile(String path, Properties properties) {
            this.path = path;
            this.properties = properties;
        }

        @Override
        public InputStream open() {
            ImmutableSortedMap.Builder<String, String> builder = ImmutableSortedMap.naturalOrder();
            for (Enumeration<?> names = properties.propertyNames(); names.hasMoreElements();) {
                String name = (String) names.nextElement();
                builder.put(name, properties.getProperty(name));
            }
            String joined = builder.build().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("\n"));
            return new ByteArrayInputStream(joined.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public FileType getType() {
            return FileType.REGULAR;
        }
    }
}
