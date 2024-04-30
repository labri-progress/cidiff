package org.github.cidiff.benchmark;

import org.github.cidiff.Action;
import org.github.cidiff.Line;
import org.github.cidiff.LogDiffer;
import org.github.cidiff.LogParser;
import org.github.cidiff.Metric;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.List;

public class Benchmark {

	private static final Path DATASET = Path.of("data/breakages/");
	private static final int LOOPS = 1;
	private static final String SUCCESS_FILE = "pass.log";
	private static final String FAILURE_FILE = "fail.log";

	public static void main(String[] args) throws IOException {
		List<Path> directories = collectDirectories();

		File file = new File("build/reports/benchmark.csv");
		if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
			throw new IllegalStateException("Cannot create directories for " + file.getParentFile().getAbsolutePath());
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		writer.write("directory,type,duration,lines-left,lines-right,actions,added,deleted,updated,moved-unchanged,moved-updated,similar-groups,similar-groups-left,similar-groups-right\n");

		LogParser parser = LogParser.Type.GITHUB.construct();
		LogDiffer seed = LogDiffer.Algorithm.SEED.construct();
		LogDiffer lcs = LogDiffer.Algorithm.LCS.construct();

		Options options = new Options();
		Options optionsEven = new Options().with(Options.EVEN_IDENTICAL, true);
		Options optionsEvenRecurse = new Options().with(Options.EVEN_IDENTICAL, true).with(Options.RECURSIVE_SEARCH, true);
		Options optionsLcs = new Options().with(Options.METRIC, Metric.EQUALITY);

		for (int i = 0; i < directories.size(); i++) {
			Path dir = directories.get(i);
			List<Line> leftLines = parser.parse(dir.resolve(SUCCESS_FILE).toString(), options);
			List<Line> rightLines = parser.parse(dir.resolve(FAILURE_FILE).toString(), options);
			if (!leftLines.isEmpty() && !rightLines.isEmpty()) {
				compute(i, directories.size(), "seed", dir, seed, leftLines, rightLines, options, writer);
				compute(i, directories.size(), "seed-even", dir, seed, leftLines, rightLines, optionsEven, writer);
				compute(i, directories.size(), "seed-recurse", dir, seed, leftLines, rightLines, optionsEvenRecurse, writer);
				compute(i, directories.size(), "lcs", dir, lcs, leftLines, rightLines, optionsLcs, writer);
			}
		}

		writer.close();
		System.out.println("done");
	}

	private static void compute(int i, int size, String type, Path dir, LogDiffer seed, List<Line> leftLines, List<Line> rightLines, Options options, BufferedWriter writer) throws IOException {
		System.out.printf("%d/%d %s %s%n", i, size, type, DATASET.relativize(dir));
		long duration = 0;
		for (int loop = 0; loop < LOOPS; loop++) {
			long b = System.currentTimeMillis();
			seed.diff(leftLines, rightLines, options);
			long a = System.currentTimeMillis();
			duration += a-b;
		}
		Pair<List<Action>> actions = seed.diff(leftLines, rightLines, options);
		Metrics metrics = metric(actions);
		writer.write("\"%s\",%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%n".formatted(
				DATASET.relativize(dir).toString(), type, (duration / LOOPS),
				leftLines.size(), rightLines.size(), metrics.actions, metrics.added, metrics.deleted,
				metrics.updated, metrics.movedUnchanged, metrics.movedUpdated,
				(metrics.similarBlockLeft + metrics.similarBlockRight), metrics.similarBlockLeft, metrics.similarBlockRight
		));
		System.gc();
	}

	private static Metrics metric(Pair<List<Action>> actions) {
		// count actions
		int[] counts = new int[5];
		counts[0] = (int) actions.right().stream().filter(a -> a.type() == Action.Type.ADDED).count();
		actions.left().forEach(a -> {
			switch (a.type()) {
				case DELETED -> counts[1]++;
				case UPDATED -> counts[2]++;
				case MOVED_UNCHANGED -> counts[3]++;
				case MOVED_UPDATED -> counts[4]++;
				case ADDED, UNCHANGED, SKIPPED, NONE -> {
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

	private record Metrics(int actions, int added, int deleted, int updated, int movedUnchanged, int movedUpdated,
	                       int similarBlockLeft, int similarBlockRight) {
	}

}
