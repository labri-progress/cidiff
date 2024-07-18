package org.github.cidiff.benchmark;

import org.github.cidiff.Action;
import org.github.cidiff.Line;
import org.github.cidiff.LogDiffer;
import org.github.cidiff.LogParser;
import org.github.cidiff.Metric;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;
import org.github.cidiff.differs.SeedDiffer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class Benchmark {

	private static final Path DATASET = Path.of("../dataset");
	private static final int LOOPS = 5;
	public static final String SUCCESS_FILE = "success.log";
	public static final String FAILURE_FILE = "failure.log";
	public static final int TIMEOUT = 60;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

	private static Pair<List<Action>> callWithTimeout(Supplier<Pair<List<Action>>> func) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Pair<List<Action>>> future = executor.submit(func::get);
		try {
			Pair<List<Action>> r = future.get(TIMEOUT, TimeUnit.SECONDS);
			executor.shutdownNow();
			return r;
		}
		catch (InterruptedException | ExecutionException | TimeoutException ignored) {
			future.cancel(true);
			executor.shutdownNow();
			return new Pair<>(new ArrayList<>(), new ArrayList<>());
		} finally {
			executor.shutdownNow();
		}
	}

	public static void main(String[] args) throws IOException {
		List<Path> directories = collectDirectories();

		File file = new File("../benchmark.csv");
		if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
			throw new IllegalStateException("Cannot create directories for " + file.getParentFile().getAbsolutePath());
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		writer.write("directory,type,duration,lines-left,lines-right,actions,added,deleted,updated,moved-unchanged,moved-updated,similar-groups,similar-groups-left,similar-groups-right\n");

		LogParser parser = LogParser.Type.GITHUB.construct();
		LogDiffer seed = LogDiffer.Algorithm.SEED.construct();
		LogDiffer lcs = LogDiffer.Algorithm.LCS.construct();

		Options optionsSeed = new Options();
		Options optionsLcs = new Options().with(Options.METRIC, Metric.EQUALITY);

		Random random = new Random(123456789);

//		int size = 100;
		int size = directories.size();
		for (int i = 0; i < size; i++) {
//			int n = random.nextInt(directories.size());
			Path dir = directories.get(i);
//			Path dir = directories.get(n);
			List<Line> leftLines = parser.parse(dir.resolve(SUCCESS_FILE).toString(), optionsSeed);
			List<Line> rightLines = parser.parse(dir.resolve(FAILURE_FILE).toString(), optionsSeed);
			if (!leftLines.isEmpty() && !rightLines.isEmpty()) {
				compute(i, size, "seed", dir, seed, leftLines, rightLines, optionsSeed, writer);
				compute(i, size, "lcs", dir, lcs, leftLines, rightLines, optionsLcs, writer);
			} else {
				System.out.printf("skipping %s: left=%d, right=%d\n", DATASET.relativize(dir), leftLines.size(), rightLines.size());
//				--i;
			}
		}

		writer.close();
		System.out.println("done");
	}

	private static void compute(int i, int size, String type, Path dir, LogDiffer seed, List<Line> leftLines, List<Line> rightLines, Options options, BufferedWriter writer) throws IOException {
		System.out.printf("%d/%d (%.1f%%) %s %s %s", i, size, i * 100.0 / size, type, DATASET.relativize(dir), FORMATTER.format(LocalTime.now()));
		List<Long> durations = new ArrayList<>();
		Pair<List<Action>> actions = Pair.of(List.of(), List.of());
		for (int loop = 0; loop < LOOPS; loop++) {
			long b = System.nanoTime();
			actions = callWithTimeout(() -> seed.diff(leftLines, rightLines, options));
			long a = System.nanoTime();
			durations.add(a - b);
			if (actions.left().isEmpty() && actions.right().isEmpty()) {
				break;
			}
		}
		if (actions.left().isEmpty() && actions.right().isEmpty()) {
			System.out.printf("\r%d/%d (%.1f%%) timeout %s %s (%s)%n", i, size, i * 100.0 / size, type, DATASET.relativize(dir), FORMATTER.format(LocalTime.now()));
			writer.write("\"%s\",%s,%.1f,%d,%d,0,0,0,0,0,0,0,0,0%n".formatted(
					DATASET.relativize(dir).toString(), type, -1.0, leftLines.size(), rightLines.size()
			));
		} else {
			durations.sort(Long::compareTo);
			Metrics metrics = metric(actions);
			System.out.printf("\r%d/%d (%.1f%%) %.2fms %s %s (%s)%n", i, size, i * 100.0 / size, (durations.get(LOOPS / 2) / 1_000_000.0), type, DATASET.relativize(dir), FORMATTER.format(LocalTime.now()));
			writer.write("\"%s\",%s,%.1f,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%n".formatted(
					DATASET.relativize(dir).toString(), type, (durations.get(LOOPS / 2) / 1_000_000.0),
					leftLines.size(), rightLines.size(), metrics.actions, metrics.added, metrics.deleted,
					metrics.updated, metrics.movedUnchanged, metrics.movedUpdated,
					(metrics.similarBlockLeft + metrics.similarBlockRight), metrics.similarBlockLeft, metrics.similarBlockRight
			));
		}
		writer.flush();
	}

	public static Metrics metric(Pair<List<Action>> actions) {
		// count actions
		int[] counts = new int[5];
		counts[0] = (int) actions.right().stream().filter(a -> a.type() == Action.Type.ADDED).count();
		actions.left().forEach(a -> {
			switch (a.type()) {
				case DELETED -> counts[1]++;
				case UPDATED -> counts[2]++;
				case MOVED_UNCHANGED -> counts[3]++;
				case MOVED_UPDATED -> counts[4]++;
				case ADDED, UNCHANGED, NONE -> {
				}
			}
		});
		// count similar blocks
		int similarLeft = 1;
		if (!actions.left().isEmpty()) {
			Action.Type last = actions.left().get(0).type();
			for (Action action : actions.left()) {
				if (action.type() != last) {
					similarLeft++;
					last = action.type();
				}
			}
		} else {
			similarLeft = 0;
		}
		int similarRight = 1;
		if (!actions.right().isEmpty()) {
			Action.Type last = actions.right().get(0).type();
			for (Action action : actions.right()) {
				if (action.type() != last) {
					similarRight++;
					last = action.type();
				}
			}
		} else {
			similarRight = 0;
		}
		return new Metrics((int) (actions.left().stream().filter(a -> a.type() != Action.Type.UNCHANGED).count() + counts[0]),
				counts[0], counts[1], counts[2], counts[3], counts[4], similarLeft, similarRight);
	}

	public static List<Path> collectDirectories() {
		List<Path> directories = new ArrayList<>();

		SimpleFileVisitor<Path> walker = new SimpleFileVisitor<>() {

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Path success = dir.resolve(SUCCESS_FILE);
				Path failure = dir.resolve(FAILURE_FILE);
				if (failure.toFile().exists() && success.toFile().exists()) {
					directories.add(dir);
				}
				return super.postVisitDirectory(dir, exc);
			}
		};

		try {
			Files.walkFileTree(DATASET, walker);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return directories;
	}

	public record Metrics(int actions, int added, int deleted, int updated, int movedUnchanged, int movedUpdated,
						   int similarBlockLeft, int similarBlockRight) {

	}

}
