package org.gradle.snapshot.rules;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.gradle.snapshot.contexts.Context;
import org.gradle.snapshot.files.FileishWithContents;
import org.gradle.snapshot.operations.Operation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

public class ProcessPropertyFile<C extends Context> extends Rule<FileishWithContents, C> {
    private static final Pattern PROPERTY_FILE = Pattern.compile(".*\\.properties");
    private final Set<String> ignoredKeys;

    public ProcessPropertyFile(Class<C> contextType, Set<String> ignoredKeys) {
        super(FileishWithContents.class, contextType, PROPERTY_FILE);
        this.ignoredKeys = ignoredKeys;
    }

    @Override
    protected void doProcess(FileishWithContents file, C context, List<Operation> operations) throws IOException {
        Properties properties = new Properties();
        properties.load(file.open());
        ImmutableSortedMap<String, String> propertiesMap = ImmutableSortedMap.copyOf(Maps.fromProperties(properties));
        Hasher hasher = Hashing.md5().newHasher();
        for (Map.Entry<String, String> property : propertiesMap.entrySet()) {
            String key = property.getKey();
            String value = property.getValue();
            if (!ignoredKeys.contains(key)) {
                hasher.putString(key + "=" + value + "\n", StandardCharsets.UTF_8);
            }
        }
        context.recordSnapshot(file, hasher.hash());
    }
}
