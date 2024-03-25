package org.github.cidiff.benchmark;

import org.github.cidiff.Action;
import org.github.cidiff.Line;
import org.github.cidiff.LogDiffer;
import org.github.cidiff.LogParser;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Benchmark {

	private static final Path DATASET = Path.of("/home/ketheroth/these/code/cidiff-next/data/breakages/");

	public static void main(String[] args) {
		List<Path> directories = collectDirectories();

		CSVWriter csvWriter = new CSVWriter("benchmark-1.csv",
				"directory", "type", "run", "duration", "lines-left", "lines-right", "actions", "added",
				"deleted", "unchanged", "updated", "moved_unchanged", "moved_updated", "similar-groups",
				"similar-groups-left", "similar-groups-right");
		Options.setup(new Properties());  // for the parser and differ init

		LogParser parser = LogParser.Type.GITHUB.construct();
		LogDiffer differ = LogDiffer.Algorithm.SEED.construct();
		int loops = 1;

		Options.setup(new Properties());
		compute(csvWriter, directories, "seed-default", loops, parser, differ);

		Properties properties2 = new Properties();
		properties2.setProperty("differ.seed.even", "true");
		Options.setup(properties2);
		compute(csvWriter, directories, "seed-even", loops, parser, differ);

		Properties properties3 = new Properties();
		properties3.setProperty("metric", "EQUALITY");
		Options.setup(properties3);
		compute(csvWriter, directories, "lcs", loops, parser, LogDiffer.Algorithm.LCS.construct());

		System.out.println("done");
		csvWriter.close();
		System.out.println("closed");
	}

	public static void compute(CSVWriter csv, List<Path> directories, String type, int loops, LogParser parser, LogDiffer differ) {
		for (int i = 0; i < directories.size(); i++) {
			Path dir = directories.get(i);
			for (int j = 0; j < loops; j++) {
				System.out.printf("%d/%d %s %d %s%n", i, directories.size(), type, j, DATASET.relativize(dir));
				List<Line> leftLines = parser.parse(dir.resolve("pass.log").toString());
				List<Line> rightLines = parser.parse(dir.resolve("fail.log").toString());
				long before = System.currentTimeMillis();
				Pair<List<Action>> actions = differ.diff(leftLines, rightLines);
				long after = System.currentTimeMillis();
				// count actions
				int[] counts = new int[6];
				counts[0] = (int) actions.right().stream().filter(a -> a.type() == Action.Type.ADDED).count();
				final int actionsCount = actions.left().size() + counts[0];
				actions.left().forEach(a -> {
					switch (a.type()) {
						case DELETED -> counts[1]++;
						case UNCHANGED -> counts[2]++;
						case UPDATED -> counts[3]++;
						case MOVED_UNCHANGED -> counts[4]++;
						case MOVED_UPDATED -> counts[5]++;
						case ADDED, SKIPPED, NONE -> {}
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
				csv.write(DATASET.relativize(directories.get(i)).toString(), type, j, (after - before), leftLines.size(), rightLines.size(),
						actionsCount, counts[0], counts[1], counts[2], counts[3], counts[4], counts[5], (similarLeft + similarRight), similarLeft, similarRight);
				System.gc();
			}
		}

	}

	public static List<Path> collectDirectories() {
		List<Path> directories = new ArrayList<>();

		SimpleFileVisitor<Path> walker = new SimpleFileVisitor<>() {

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Path failure = dir.resolve("fail.log");
				Path success = dir.resolve("pass.log");
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

	private static Pair<List<Action>> callWithTimeout(Supplier<Pair<List<Action>>> func, int duration) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Pair<List<Action>>> future = executor.submit(func::get);
		try {
			return future.get(duration, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException ignored) {
			System.out.println("timeout");
			future.cancel(true);
			return new Pair<>(new ArrayList<>(), new ArrayList<>());
		} finally {
			executor.shutdownNow();
		}
	}

	public record Data(int i, Path path, long start, long end, Pair<List<Action>> actions) {
		Data(int i, Path path) {
			this(i, path, -1, -1, null);
		}

		public Data withActions(long start, long end, Pair<List<Action>> actions) {
			return new Data(this.i, this.path, start, end, actions);
		}
	}
	public static void computeParallel(CSVWriter csv, List<Path> directories, String type, int loops, LogParser parser, LogDiffer differ) {
		AtomicInteger counter = new AtomicInteger();
		List<String> csvString = IntStream.range(0, directories.size())
				.parallel()
				.mapToObj(index -> new Data(index, directories.get(index)))
				.map(data -> {
					List<Line> leftLines = parser.parse(data.path.resolve("pass.log").toString());
					List<Line> rightLines = parser.parse(data.path.resolve("fail.log").toString());
					int i = counter.incrementAndGet();
					System.out.printf("%d/%d %s%n", i, directories.size(), type);
					long before = System.currentTimeMillis();
					Pair<List<Action>> actions = differ.diff(leftLines, rightLines);
					long after = System.currentTimeMillis();
					return data.withActions(before, after, actions);
				})
				.filter(data -> !data.actions.left().isEmpty() && !data.actions.right().isEmpty())
				.map(data -> {
					Pair<List<Action>> actions = data.actions;
					int[] counts = new int[6];
					counts[1] = (int) actions.right().stream().filter(a -> a.type() == Action.Type.ADDED).count();
					int actionsCount = actions.left().size() + counts[1];
					actions.left().forEach(a -> {
						switch (a.type()) {
							case DELETED -> counts[0]++;
							case UPDATED -> counts[2]++;
							case UNCHANGED -> counts[3]++;
							case MOVED_UPDATED -> counts[4]++;
							case MOVED_UNCHANGED -> counts[5]++;
							case ADDED, SKIPPED, NONE -> {}
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
					return "\"%s\",%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%n".formatted(DATASET.relativize(directories.get(data.i)).toString(), type,
							(data.end - data.start), data.actions.left().size(), data.actions.right().size(),
							actionsCount, counts[1], counts[0], counts[3], counts[2], counts[5], counts[4],
							(similarLeft + similarRight), similarLeft, similarRight);

				})
				.toList();
		System.out.println("nb of items: " + csvString.size());
		csvString.forEach(csv::write);
	}

}
