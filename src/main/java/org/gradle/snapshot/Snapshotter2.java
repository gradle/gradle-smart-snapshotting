package org.gradle.snapshot;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

	public <C extends Context> C snapshot(Collection<? extends File> files, C context, Iterable<? extends Rule> rules) throws IOException {
		process(files.stream()
				.map(file -> Physical.of(file.getName(), file))
				.collect(Collectors.toList()), context, rules);
		return context;
	}

	public void process(Collection<? extends Fileish> files, Context rootContext, Iterable<? extends Rule> rules) throws IOException {

		ArrayDeque<Operation> queue = Queues.newArrayDeque();
		SnapshotterState state = new SnapshotterState(rootContext, rules);
		files.forEach((Fileish file) -> queue.addLast(new ApplyTo(file, rootContext)));

		List<Operation> dependencies = Lists.newArrayList();

		while (!queue.isEmpty()) {
			Operation operation = queue.peek();
			boolean finished = operation.isFinished();

			if (!finished) {
				operation.setContextIfNecessary(state);

				dependencies.clear();
				operation.execute(state, dependencies);

				finished = dependencies.isEmpty();
				if (!finished) {
					for (Operation dependency : Lists.reverse(dependencies)) {
						if (!dependency.isFinished()) {
							queue.push(dependency);
						}
					}
				}
			}

			if (finished) {
				operation.close();
				queue.remove();
			}
		}
	}

	static class SnapshotterState {
		private Context context;
		private final Iterable<? extends Rule> rules;

		public SnapshotterState(Context context, Iterable<? extends Rule> rules) {
			this.context = context;
			this.rules = rules;
		}

		public Context getContext() {
			return context;
		}

		public void setContext(Context context) {
			this.context = context;
		}

		public Iterable<? extends Rule> getRules() {
			return rules;
		}
	}

	static abstract class Operation implements Closeable {
		private Context context;

		public Operation(Context context) {
			this.context = context;
		}

		public Context getContext() {
			if (context == null) {
				throw new IllegalStateException("No context is specified");
			}
			return context;
		}

		void setContextIfNecessary(SnapshotterState state) {
			if (this.context == null) {
				this.context = state.getContext();
			} else {
				state.setContext(this.context);
			}
		}

		public void execute(SnapshotterState state, List<Operation> dependencies) throws IOException {
		}

		public boolean isFinished() throws IOException {
			return false;
		}

		@Override
		public void close() throws IOException {
		}
	}

	static class ApplyTo extends Operation {
		private final Fileish file;
		private boolean executed;

		public ApplyTo(Fileish file) {
			this(file, null);
		}

		public ApplyTo(Fileish file, Context context) {
			super(context);
			this.file = file;
		}

		@Override
		public void execute(SnapshotterState state, List<Operation> dependencies) throws IOException {
			Context context = getContext();
			for (Rule rule : state.getRules()) {
				if (rule.matches(file, context)) {
					executed = true;
					rule.process(file, context, dependencies);
					return;
				}
			}
			throw new IllegalStateException(String.format("Cannot find matching rule for %s in context %s", file, context));
		}

		@Override
		public boolean isFinished() throws IOException {
			return executed;
		}
	}

	static abstract class Rule {
		private final Class<? extends Context> contextType;
		private final Class<? extends Fileish> fileType;
		private final Pattern pathMatcher;

		public Rule(Class<? extends Fileish> fileType, Class<? extends Context> contextType, Pattern pathMatcher) {
			this.contextType = contextType;
			this.fileType = fileType;
			this.pathMatcher = pathMatcher;
		}

		public boolean matches(Fileish file, Context context) {
			return contextType.isAssignableFrom(context.getType())
					&& fileType.isAssignableFrom(file.getClass())
					&& (pathMatcher == null || pathMatcher.matcher(file.getPath()).matches());
		}

		public abstract void process(Fileish file, Context context, List<Operation> dependencies) throws IOException;
	}

	static abstract class FileRule extends Rule {
		public FileRule(Class<? extends Context> contextType, Pattern pathMatcher) {
			super(FileishWithContents.class, contextType, pathMatcher);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void process(Fileish file, Context context, List<Operation> dependencies) throws IOException {
			processInternal((FileishWithContents) file, context, dependencies);
		}

		abstract protected void processInternal(FileishWithContents file, Context context, List<Operation> dependencies) throws IOException;
	}

	static class ProcessZip extends Operation {
		private final FileishWithContents file;
		private ZipInputStream input;

		public ProcessZip(FileishWithContents file, Context context) {
			super(context);
			this.file = file;
		}

		@Override
		public void execute(SnapshotterState state, List<Operation> dependencies) throws IOException {
			if (input == null) {
				input = new ZipInputStream(file.open());
			}
			ZipEntry entry = input.getNextEntry();
			if (entry == null) {
				return;
			}

			// Match against directories
			String path = entry.getName();
			int index = -1;
			while ((index = path.indexOf('/', index + 1)) != -1) {
				dependencies.add(new ApplyTo(new ZipEntryDirectory(path.substring(0, index))));
			}

			// Match against the file
			if (!entry.isDirectory()) {
				dependencies.add(new ApplyTo(new ZipEntryFile(path, input)));
			}
		}

		@Override
		public void close() throws IOException {
			System.out.println("Closing " + file.getPath());
			if (input != null) {
				ZipInputStream inputToClose = this.input;
				this.input = null;
				inputToClose.close();
			}
		}
	}

	static class ProcessDirectory extends Operation {
		private final PhysicalDirectory root;
		private Iterator<File> files;

		public ProcessDirectory(PhysicalDirectory root, Context context) {
			super(context);
			this.root = root;
		}

		@Override
		public void execute(SnapshotterState state, List<Operation> dependencies) throws IOException {
			File rootFile = root.getFile();
			if (files == null) {
				files = Files.walk(rootFile.toPath()).map(Path::toFile).iterator();
			}

			while (files.hasNext()) {
				File file = files.next();

				// Do not process the root file
				if (file.equals(rootFile)) {
					continue;
				}
				applyToAncestry(new StringBuilder(), rootFile, file, dependencies);
				break;
			}
		}

		private void applyToAncestry(StringBuilder path, File root, File file, List<Operation> dependencies) {
			File parent = file.getParentFile();
			if (!parent.equals(root)) {
				applyToAncestry(path, root, parent, dependencies);
				path.append('/');
			}
			path.append(file.getName());
			dependencies.add(new ApplyTo(Physical.of(path.toString(), file)));
		}
	}

	static class RuntimeClasspathContext extends AbstractContext {}

	static class RuntimeClasspathEntryContext extends AbstractContext {
		@Override
		protected HashCode fold(Stream<Map.Entry<String, Result>> results) {
			return super.fold(results.sorted(comparing(Map.Entry::getKey)));
		}
	}

	static class DefaultSnapshotRule extends FileRule {
		public DefaultSnapshotRule(Class<? extends Context> contextType, Pattern pathMatcher) {
			super(contextType, pathMatcher);
		}

		@Override
		public void processInternal(FileishWithContents file, Context context, List<Operation> dependencies) throws IOException {
			try (InputStream input = file.open()) {
				Hasher hasher = md5().newHasher();
				copy(input, asOutputStream(hasher));
				context.snapshot(file, hasher.hash());
			}
		}
	}

	interface Fileish {
		String getPath();
	}

	interface Directoryish extends Fileish {
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

	interface Physical extends Fileish {
		File getFile();

		static Physical of(String path, File file) {
			if (file.isDirectory()) {
				return new PhysicalDirectory(path, file);
			} else {
				return new PhysicalFile(path, file);
			}
		}
	}

	static class PhysicalFile extends AbstractFileish implements FileishWithContents, Physical {
		private final File file;

		public PhysicalFile(String path, File file) {
			super(path);
			this.file = file;
		}

		@Override
		public File getFile() {
			return file;
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

	static class PhysicalDirectory extends AbstractFileish implements Physical, Directoryish {
		private final File file;

		public PhysicalDirectory(String path, File file) {
			super(path);
			this.file = file;
		}

		@Override
		public File getFile() {
			return file;
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

	static class ZipEntryDirectory extends AbstractFileish implements Directoryish {
		public ZipEntryDirectory(String path) {
			super(path);
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
}
