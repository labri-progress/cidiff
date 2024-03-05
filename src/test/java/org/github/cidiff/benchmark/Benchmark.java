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
import java.util.function.Supplier;

public class Benchmark {
	public static void main(String[] args) {
		List<Path> directories = collectDirectories();
		CSVWriter csvWriter = new CSVWriter("build/reports/benchmark.csv", "directory", "type", "run", "duration", "lines-left", "lines-right", "actions", "added", "deleted", "unchanged", "updated", "moved_unchanged", "moved_updated", "similar-groups", "similar-groups-left", "similar-groups-right");
		Options.setup(new Properties());  // for the parser and differ init

		LogParser parser = LogParser.Type.GITHUB.construct();
		LogDiffer differ = LogDiffer.Algorithm.SEED.construct();
		int loops = 1;

		Options.setup();
		compute(csvWriter, directories, "seed-default", loops, parser, differ);

		Properties differSeedProperty = new Properties();
		differSeedProperty.setProperty("differ.seed.even", "true");
		Options.setup(differSeedProperty);

		//compute(csvWriter, directories, "seed-even", loops, parser, differ);

		/*
		for (String metric : List.of("EQUALITY", "JARO_WINKLER", "LEVENSHTEIN", "COSINE", "MONGE_ELKMAN", "SMITH_WATERMAN", "JACCARD")) {
			Properties properties3 = new Properties();
			properties3.setProperty("metric", metric);
			Options.setup(properties3);
			compute(csvWriter, directories, "seed-"+metric.toLowerCase(), loops, parser, differ);
		}

		compute(csvWriter, directories, "lcs-equality", loops, parser, LogDiffer.Algorithm.LCS.construct());

		compute(csvWriter, directories, "lcs-equality-no-parse", loops, LogParser.Type.TRIMMING.construct(), LogDiffer.Algorithm.LCS.construct());

		Options.setup(new Properties());
		compute(csvWriter, directories, "lcs-logsim-no-parse", loops, LogParser.Type.TRIMMING.construct(), LogDiffer.Algorithm.LCS.construct());
		*/

		csvWriter.close();
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
			Files.walkFileTree(Path.of("data/breakages/"), walker);
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
		}
		catch (InterruptedException | ExecutionException | TimeoutException ignored) {
			future.cancel(true);
			return new Pair<>(new ArrayList<>(), new ArrayList<>());
		}
		finally {
			executor.shutdownNow();
		}
	}

	public static void compute(CSVWriter csv, List<Path> directories, String type, int loops, LogParser parser, LogDiffer differ) {
		Path datasetPath = Path.of("data/breakages/");
		for (int i = 0; i < directories.size(); i++) {
			Path dir = directories.get(i);
			for (int j = 0; j < loops; j++) {
				System.out.printf("%d/%d %s %d %s%n", i, directories.size(), type, j, datasetPath.relativize(dir));
				List<Line> leftLines = parser.parse(dir.resolve("pass.log").toString());
				List<Line> rightLines = parser.parse(dir.resolve("fail.log").toString());
				long before = System.currentTimeMillis();
				Pair<List<Action>> actions = callWithTimeout(() -> differ.diff(leftLines, rightLines), 10);
				long after = System.currentTimeMillis();
				if (actions.left().isEmpty() && actions.right().isEmpty()) {
					System.out.println("skipped");
					break;
				}
				// count actions
				int actionsCount = (int) (actions.left().size() + actions.right().stream().filter(a -> a.type() == Action.Type.ADDED).count());
				int deleted = (int) actions.left().stream().filter(a -> a.type() == Action.Type.DELETED).count();
				int added = (int) actions.right().stream().filter(a -> a.type() == Action.Type.ADDED).count();
				int updated = (int) actions.left().stream().filter(a -> a.type() == Action.Type.UPDATED).count();
				int unchanged = (int) actions.left().stream().filter(a -> a.type() == Action.Type.UNCHANGED).count();
				int moved_updated = (int) actions.left().stream().filter(a -> a.type() == Action.Type.MOVED_UPDATED).count();
				int moved_unchanged = (int) actions.left().stream().filter(a -> a.type() == Action.Type.MOVED_UNCHANGED).count();
				// count similar blocks
				int similarLeft = 1;
				Action.Type last = actions.left().get(0).type();
				for (Action action : actions.left()) {
					if (action.type() != last) {
						similarLeft++;
						last = action.type();
					}
				}
				int similarRight = 1;
				last = actions.right().get(0).type();
				for (Action action : actions.right()) {
					if (action.type() != last) {
						similarRight++;
						last = action.type();
					}
				}
				csv.write(datasetPath.relativize(directories.get(i)).toString(), type, j, (after - before), leftLines.size(), rightLines.size(), actionsCount, added, deleted, unchanged, updated, moved_unchanged, moved_updated, (similarLeft + similarRight), similarLeft, similarRight);

			}
		}
	}
}
