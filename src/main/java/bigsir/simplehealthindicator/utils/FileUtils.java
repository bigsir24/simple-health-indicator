package bigsir.simplehealthindicator.utils;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public final class FileUtils {
	private FileUtils() {}

	public static void walkEach(@NonNull final Path dirPath, @NonNull final Consumer<Path> consumer) {
		try (final Stream<Path> paths = Files.walk(dirPath)) {
			paths.forEach(consumer);
		} catch (IOException ignored) {}
	}

	public static @NonNull String getExtension(@NonNull final Path path) {
		if (path.getFileName() == null) return "";

		final String fileName = path.getFileName().toString();
		final int last = fileName.lastIndexOf('.');
		if (last == -1) return "";

		return fileName.substring(last + 1);
	}

	public static @NonNull String getName(@NonNull final Path path) {
		if (path.getFileName() == null) return "";

		final String fileName = path.getFileName().toString();
		final int last = fileName.lastIndexOf('.');
		return fileName.substring(0, last == -1 ? fileName.length() : last);
	}

	public static @Nullable Path findFirst(@NonNull final Path path, final int maxDepth, @NonNull final BiPredicate<Path, BasicFileAttributes> predicate) {
		try {
			try (Stream<Path> pathStream = Files.find(path, maxDepth, predicate)) {
				return pathStream.findFirst().orElse(null);
			}
		}catch (IOException ignored) {
			return null;
		}
	}

	public static @Nullable Path findFirst(@NonNull final Path path, @NonNull final BiPredicate<Path, BasicFileAttributes> predicate) {
		return findFirst(path, Integer.MAX_VALUE, predicate);
	}
}
