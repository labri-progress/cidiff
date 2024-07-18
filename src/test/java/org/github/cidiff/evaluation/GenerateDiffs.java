package org.github.cidiff.evaluation;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import org.github.cidiff.Action;
import org.github.cidiff.DiffClient;
import org.github.cidiff.Line;
import org.github.cidiff.LogDiffer;
import org.github.cidiff.LogParser;
import org.github.cidiff.Metric;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;
import org.github.cidiff.benchmark.Benchmark;
import static org.github.cidiff.benchmark.Benchmark.SUCCESS_FILE;
import static org.github.cidiff.benchmark.Benchmark.FAILURE_FILE;

public class GenerateDiffs {

	public static void main(String[] args) {
		List<Path> directories = Benchmark.collectDirectories();
		LogParser parser = LogParser.Type.GITHUB.construct();
		LogDiffer differSeed = LogDiffer.Algorithm.SEED.construct();
		LogDiffer differLcs = LogDiffer.Algorithm.LCS.construct();

		Options optionsSeed = new Options();
		Options optionsLcs = new Options().with(Options.METRIC, Metric.EQUALITY);

		File fileSeed = new File("output/alpha/");
		fileSeed.mkdirs();
		File fileLcs = new File("output/beta/");
		fileLcs.mkdirs();
		System.out.printf("directories found: %d%n", directories.size());
		long seed = 123456L;
		Random random = new Random(seed);
		for (int i = 0; i < 100; i++) {
			int n = random.nextInt(directories.size());
			Path dir = directories.get(n);
			List<Line> leftLines = parser.parse(dir.resolve(SUCCESS_FILE).toString(), optionsSeed);
			List<Line> rightLines = parser.parse(dir.resolve(FAILURE_FILE).toString(), optionsSeed);
			if (!leftLines.isEmpty() && !rightLines.isEmpty() && leftLines.size() < 30_000 && rightLines.size() < 30_000) {
				System.out.printf("%d %s", i, dir.toString());
				System.out.printf(" seed");
				diffIt(differSeed, leftLines, rightLines, optionsSeed.with(Options.OUTPUT_PATH, "output/alpha/diff" + i + ".html"));
				System.out.printf(" lcs");
				diffIt(differLcs, leftLines, rightLines, optionsLcs.with(Options.OUTPUT_PATH, "output/beta/diff" + i + ".html"));
				System.out.println();
			} else {
				--i;
			}
		}

	}

	public static void diffIt(LogDiffer differ, List<Line> left, List<Line> right, Options options) {
		Pair<List<Action>> actionsSeed = differ.diff(left, right, options);
		var lines = new Pair<>(left, right);
		DiffClient client = DiffClient.Type.FILTERED.construct(lines, actionsSeed);
		client.execute(options);
	}

}
