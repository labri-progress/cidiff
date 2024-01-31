package org.cidiff.cidiff;

import org.cidiff.cidiff.clients.LogsPanel;

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

public class BenchmarkMergeSeeds {
	public static void main(String[] args) {
		Options.setup(new Properties());  // defaults options are fine
		Path datasetPath = Path.of("/home/ketheroth/these/cidiff/data/breakages/");

		LogParser parser = LogParser.Type.GITHUB.construct();
		LogDiffer differ = LogDiffer.Algorithm.SEED.construct();

		List<Path> directories = collectDirectories(datasetPath);

		CSVBuilder csvBuilder = new CSVBuilder("directory", "duration", "actions", "indels", "upuns");
		for (int i = 0; i < directories.size(); i++) {
			Path dir = directories.get(i);
//			if (dir.toString().contains("juliac")) {
//				// skip juliac because I don't have enough memory to compute the lcs diff
//				continue;
//			}
			System.out.printf("%d/%d %s%n", i, directories.size(), datasetPath.relativize(dir));
			List<Line> leftLines = parser.parse(dir.resolve("pass.log").toString());
			List<Line> rightLines = parser.parse(dir.resolve("fail.log").toString());
			long before = System.currentTimeMillis();
			Pair<List<Action>> actions = differ.diff(leftLines, rightLines);
			long after = System.currentTimeMillis();
			int actionsCount = (int) (actions.left().size() + actions.right().stream().filter(a -> a.type() == Action.Type.ADDED).count());
			int indels = (int) (actions.left().stream().filter(a -> a.type() == Action.Type.DELETED).count()
					+ actions.right().stream().filter(a -> a.type() == Action.Type.ADDED).count());
			int upuns = (int) (actions.left().stream().filter(a -> a.type() == Action.Type.UPDATED).count()
					+ actions.left().stream().filter(a -> a.type() == Action.Type.UNCHANGED).count()
					+ actions.left().stream().filter(a -> a.type() == Action.Type.MOVED_UPDATED).count()
					+ actions.left().stream().filter(a -> a.type() == Action.Type.MOVED_UNCHANGED).count());
			csvBuilder.add(datasetPath.relativize(directories.get(i)).toString(), (after - before), actionsCount, indels, upuns);
		}

		String csv = csvBuilder.build();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("benchmark-merge-seeds-without.csv"));
			writer.write(csv);
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<Path> collectDirectories(Path datasetPath) {
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
			Files.walkFileTree(datasetPath, walker);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return directories;
	}
}
