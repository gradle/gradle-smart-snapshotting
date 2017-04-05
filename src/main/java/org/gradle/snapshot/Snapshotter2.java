package org.gradle.snapshot;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.apache.commons.io.input.CloseShieldInputStream;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.google.common.hash.Funnels.asOutputStream;
import static com.google.common.hash.Hashing.md5;
import static com.google.common.io.ByteStreams.copy;
import static java.util.Comparator.comparing;

public class Snapshotter2 {
	public <C extends Context> C snapshot(Collection<? extends File> files, Class<C> contextType, Iterable<? extends Rule> rules) throws IOException {
		C context;
		try {
			context = contextType.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
		return snapshot(files, context, rules);
	}

	protected <C extends Context> C snapshot(Collection<? extends File> files, C context, Iterable<? extends Rule> rules) throws IOException {
		Enumerator enumerator = new SimpleEnumerator(files.stream().map(file -> new PhysicalFile(file.getName(), file)).iterator());
		process(enumerator, context, rules);
		return context;
	}

	private void process(Enumerator enumerator, Context context, Iterable<? extends Rule> rules) throws IOException {
		while (true) {
			Fileish file = enumerator.next();
			if (file == null) {
				enumerator.close();
				break;
			}

			for (Rule rule : rules) {
				if (rule.matches(file, context)) {
					Operation operation = rule.process(file, context);
					if (operation != null) {
						process(operation.enumerator, operation.context, rules);
					}
					break;
				}
			}
		}
	}

	interface Rule {
		boolean matches(Fileish file, Context context);
		Operation process(Fileish file, Context context) throws IOException;
	}

	static class Operation {
		private final Enumerator enumerator;
		private final Context context;

		public Operation(Enumerator enumerator, Context context) {
			this.enumerator = enumerator;
			this.context = context;
		}
	}

	static abstract class AbstractRule<F extends Fileish, C extends Context> implements Rule {
		private final Class<? extends C> contextType;
		private final Class<F> fileType;
		private final Pattern pathMatcher;

		public AbstractRule(Class<F> fileType, Class<? extends C> contextType, Pattern pathMatcher) {
			this.contextType = contextType;
			this.fileType = fileType;
			this.pathMatcher = pathMatcher;
		}

		@Override
		public boolean matches(Fileish file, Context context) {
			return contextType.isAssignableFrom(context.getType())
					&& fileType.isAssignableFrom(file.getClass())
					&& (pathMatcher == null || pathMatcher.matcher(file.getPath()).matches());
		}

		@Override
		@SuppressWarnings("unchecked")
		public Operation process(Fileish file, Context context) throws IOException {
			return processInternal((F) file, context);
		}

		abstract public Operation processInternal(F file, Context context) throws IOException;
	}

	interface Enumerator extends Closeable {
		@Nullable
		Fileish next() throws IOException;
		@Override
		default void close() throws IOException {
		}
	}

	static class SimpleEnumerator implements Enumerator {
		private final Iterator<? extends Fileish> files;

		public SimpleEnumerator(Iterator<? extends Fileish> files) {
			this.files = files;
		}

		@Override
		public Fileish next() throws IOException {
			if (files.hasNext()) {
				return files.next();
			} else {
				return null;
			}
		}
	}

	static class ZipEnumerator implements Enumerator {
		private final FileishWithContents file;
		private ZipInputStream input;

		public ZipEnumerator(FileishWithContents file) {
			this.file = file;
		}

		@Override
		public Fileish next() throws IOException {
			if (input == null) {
				input = new ZipInputStream(file.open());
			}
			ZipEntry nextEntry = input.getNextEntry();
			if (nextEntry == null) {
				close();
				return null;
			}
			return new ZipEntryFile(nextEntry.getName(), input);
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

	static class RuntimeClasspathContext extends AbstractContext {}

	static class RuntimeClasspathEntryContext extends AbstractContext {
		@Override
		protected HashCode fold(Stream<Map.Entry<String, Result>> results) {
			return super.fold(results.sorted(comparing(Map.Entry::getKey)));
		}
	}

	static class DefaultSnapshotRule extends AbstractRule<FileishWithContents, Context> {
		public DefaultSnapshotRule(Class<? extends Context> contextType, Pattern pathMatcher) {
			super(FileishWithContents.class, contextType, pathMatcher);
		}

		@Override
		public Operation processInternal(FileishWithContents file, Context context) throws IOException {
			try (InputStream input = file.open()) {
				Hasher hasher = md5().newHasher();
				copy(input, asOutputStream(hasher));
				context.snapshot(file, hasher.hash());
			}
			return null;
		}
	}

	interface Fileish {
		String getPath();
	}

	abstract static class AbstractFileish implements Fileish {
		private final String path;

		public AbstractFileish(String path) {
			this.path = path;
		}

		@Override
		public String getPath() {
			return path;
		}

		@Override
		public String toString() {
			return path;
		}
	}

	interface FileishWithContents extends Fileish {
		InputStream open() throws IOException;
	}

	static class PhysicalFile extends AbstractFileish implements FileishWithContents {
		private final File file;

		public PhysicalFile(String path, File file) {
			super(path);
			this.file = file;
		}

		@Override
		public InputStream open() {
			try {
				return new BufferedInputStream(Files.newInputStream(file.toPath()));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	static class ZipEntryFile extends AbstractFileish implements FileishWithContents {
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

	public interface Context {
		void snapshot(Fileish file, HashCode hash);
		<C extends Context> C subContext(Fileish file, Class<C> type);
		Class<? extends Context> getType();
		HashCode fold();
	}

	static abstract class AbstractContext implements Context {
		@VisibleForTesting
		final Map<String, Result> results = Maps.newLinkedHashMap();

		@Override
		public Class<? extends Context> getType() {
			return getClass();
		}

		@Override
		public void snapshot(Fileish file, HashCode hash) {
			String path = file.getPath();
			results.put(path, new SnapshotResult(hash));
		}

		@Override
		public <C extends Context> C subContext(Fileish file, Class<C> type) {
			String path = file.getPath();
			Result result = results.get(path);
			C subContext;
			if (result == null) {
				try {
					subContext = type.newInstance();
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
				results.put(path, new SubContextResult(subContext));
			} else if (result instanceof SubContextResult) {
				Context resultSubContext = ((SubContextResult) result).subContext;
				subContext = type.cast(resultSubContext);
			} else {
				throw new IllegalStateException("Already has a non-context entry under path " + path);
			}
			return subContext;
		}

		@Override
		public final HashCode fold() {
			return fold(results.entrySet().stream());
		}

		protected HashCode fold(Stream<Map.Entry<String, Result>> results) {
			Hasher hasher = Hashing.md5().newHasher();
			results.forEach(entry -> {
				hasher.putString(entry.getKey(), Charsets.UTF_8);
				hasher.putBytes(entry.getValue().getHashCode().asBytes());
			});
			return hasher.hash();
		}

		@Override
		public String toString() {
			return getClass().getSimpleName();
		}

		interface Result {
			HashCode getHashCode();
		}

		static class SnapshotResult implements Result {
			private final HashCode hashCode;

			public SnapshotResult(HashCode hashCode) {
				this.hashCode = hashCode;
			}

			@Override
			public HashCode getHashCode() {
				return hashCode;
			}
		}

		static class SubContextResult implements Result {
			private final Context subContext;

			public SubContextResult(Context subContext) {
				this.subContext = subContext;
			}

			@Override
			public HashCode getHashCode() {
				return subContext.fold();
			}
		}
	}

	/**
	 * Indicates that the value of an element can be null.
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
	public @interface Nullable {}
}
