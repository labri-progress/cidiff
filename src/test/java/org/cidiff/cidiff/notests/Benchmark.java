package org.cidiff.cidiff.notests;

import org.cidiff.cidiff.Action;
import org.cidiff.cidiff.Line;
import org.cidiff.cidiff.LogDiffer;
import org.cidiff.cidiff.LogParser;
import org.cidiff.cidiff.Options;
import org.cidiff.cidiff.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Benchmark {
	public static void compute(CSVBuilder csv, List<Path> directories, String type) {
		Path datasetPath = Path.of("data/breakages/");
		LogParser parser = LogParser.Type.GITHUB.construct();
		LogDiffer differ = LogDiffer.Algorithm.SEED.construct();
		for (int i = 0; i < directories.size(); i++) {
			Path dir = directories.get(i);
			for (int j = 0; j < 10; j++) {
				System.out.printf("%d/%d %s %s %d%n", i, directories.size(), datasetPath.relativize(dir), type, j);
				List<Line> leftLines = parser.parse(dir.resolve("pass.log").toString());
				List<Line> rightLines = parser.parse(dir.resolve("fail.log").toString());
				long before = System.currentTimeMillis();
				Pair<List<Action>> actions = differ.diff(leftLines, rightLines);
				long after = System.currentTimeMillis();
				int actionsCount = (int) (actions.left().size() + actions.right().stream().filter(a -> a.type() == Action.Type.ADDED).count());
				int deleted = (int) actions.left().stream().filter(a -> a.type() == Action.Type.DELETED).count();
				int added = (int) actions.right().stream().filter(a -> a.type() == Action.Type.ADDED).count();
				int updated = (int) actions.left().stream().filter(a -> a.type() == Action.Type.UPDATED).count();
				int unchanged = (int) actions.left().stream().filter(a -> a.type() == Action.Type.UNCHANGED).count();
				int moved_updated = (int) actions.left().stream().filter(a -> a.type() == Action.Type.MOVED_UPDATED).count();
				int moved_unchanged = (int) actions.left().stream().filter(a -> a.type() == Action.Type.MOVED_UNCHANGED).count();
				csv.add(datasetPath.relativize(directories.get(i)).toString(), type, j, (after - before), actionsCount, added, deleted, unchanged, updated, moved_unchanged, moved_updated);

			}
		}
	}

	public static void main(String[] args) {
		List<Path> directories = collectDirectories();
		CSVBuilder csvBuilder = new CSVBuilder("directory", "type", "run", "duration", "actions", "added", "deleted", "unchanged", "updated", "moved_unchanged", "moved_updated");

//		Options.setup(new Properties());
//		compute(csvBuilder, directories, "default");
//
//		Properties properties = new Properties();
//		properties.setProperty(Options.Names.DIFFER_RECURSIVE_SEARCH, "true");
//		Options.setup(properties);
//		compute(csvBuilder, directories, "recurse");
//
//		Properties properties2 = new Properties();
//		properties2.setProperty(Options.Names.DIFFER_EVEN_IDENTICAL, "true");
//		Options.setup(properties2);
//		compute(csvBuilder, directories, "even");

		String csv = csvBuilder.build();
		try {
			Path generated = Path.of("generated");
			if (!Files.exists(generated)) {
				Files.createDirectory(generated);
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter("generated/benchmark.csv"));
			writer.write(csv);
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
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
			Files.walkFileTree(Path.of("data/breakages/"), walker);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return directories;
	}
}
