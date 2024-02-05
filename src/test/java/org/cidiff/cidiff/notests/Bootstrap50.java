package org.cidiff.cidiff.notests;

import org.cidiff.cidiff.Action;
import org.cidiff.cidiff.Line;
import org.cidiff.cidiff.LogDiffer;
import org.cidiff.cidiff.LogParser;
import org.cidiff.cidiff.Options;
import org.cidiff.cidiff.Pair;
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
import java.util.Random;

public class Bootstrap50 {

	public static void main(String[] args) {
		differenceInQuantityOfBlock2();

	}

	public static void selectLinesToAnnotate() {
		Options.setup(new Properties());  // defaults options are fine

		Path datasetPath = Path.of("/home/ketheroth/these/cidiff/data/breakages/");

		LogParser parser = LogParser.Type.GITHUB.construct();
		LogDiffer differ = LogDiffer.Algorithm.SEED.construct();


		int LINES_TO_SELECT = 50;

		List<Path> directories = collectDirectories(datasetPath);
		List<Action> selectedActions = new ArrayList<>();
		List<BiValue<Path, Action>> selected = new ArrayList<>();

		Random rnd = new Random(123456789);

		for (int i = 0; i < LINES_TO_SELECT; i++) {
			Path dir = directories.get(rnd.nextInt(directories.size()));
			List<Line> leftLines = parser.parse(dir.resolve("pass.log").toString());
			List<Line> rightLines = parser.parse(dir.resolve("fail.log").toString());
			Pair<List<Action>> actions = differ.diff(leftLines, rightLines);

			List<Action> updated = actions.left().stream()
					.filter(action -> action.type() == Action.Type.UPDATED)
					.toList();
			if (updated.isEmpty()) {
				System.err.printf("%s has no updated action%n", datasetPath.relativize(dir));
				i--;
				continue;
			}
			Action action;
			do {
				action = updated.get(rnd.nextInt(updated.size()));
				// TODO: 11/29/23 nhubner should stop after x attemps
			} while (selectedActions.contains(action));
			selectedActions.add(action);
			selected.add(new BiValue<>(dir, action));
			System.out.printf("selected %d : %s %d-%d%n", i, datasetPath.relativize(dir), action.left().index(), action.right().index());
		}
		CSVBuilder csvBuilder = new CSVBuilder("directory", "left", "right");
		selected.sort((val1, val2) -> {
			int i = val1.a().compareTo(val2.a());
			if (i == 0) {
				return val1.b().left().index() - val2.b().left().index();
			}
			return i;
		});
		for (BiValue<Path, Action> pathActionBiValue : selected) {
			Path path = pathActionBiValue.a();
			Action action = pathActionBiValue.b();
			csvBuilder.add(datasetPath.relativize(path).toString(), action.left().index(), action.right().index());
		}
		String csv = csvBuilder.build();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("bootstrap50-selection.csv"));
			writer.write(csv);
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static void differenceInQuantityOfBlock() {
		Options.setup(new Properties());  // defaults options are fine

		Path datasetPath = Path.of("/home/ketheroth/these/cidiff/data/breakages/");

		LogParser parser = LogParser.Type.GITHUB.construct();
		LogDiffer differSeed = LogDiffer.Algorithm.SEED.construct();
		LogDiffer differLcs = LogDiffer.Algorithm.LCS.construct();



		List<Path> directories = collectDirectories(datasetPath);

		List<Integer> linesLeft = new ArrayList<>();
		List<Integer> linesRight = new ArrayList<>();
		List<Integer> blocksSeed = new ArrayList<>();
		List<Integer> blocksLcs = new ArrayList<>();
		List<Integer> countActionsSeed = new ArrayList<>();
		List<Integer> countActionsLcs = new ArrayList<>();

		for (int i = 0; i < directories.size(); i++) {
			Path dir = directories.get(i);
			if (dir.toString().contains("juliac")) {
				// skip juliac because I don't have enough memory to compute the lcs diff
				linesLeft.add(0);
				linesRight.add(0);
				blocksSeed.add(0);
				blocksLcs.add(0);
				countActionsSeed.add(0);
				countActionsLcs.add(0);
				continue;
			}
			System.out.printf("%d/%d %s", i, directories.size(), datasetPath.relativize(dir));
			List<Line> leftLines = parser.parse(dir.resolve("pass.log").toString());
			List<Line> rightLines = parser.parse(dir.resolve("fail.log").toString());
			Pair<List<Action>> actionsSeed = differSeed.diff(leftLines, rightLines);
			Pair<List<Action>> actionsLcs = differLcs.diff(leftLines, rightLines);
			LogsPanel.insertLinesForParallelScrolling(new Pair<>(leftLines, rightLines), actionsSeed);
			LogsPanel.insertLinesForParallelScrolling(new Pair<>(leftLines, rightLines), actionsLcs);
			linesLeft.add(leftLines.size());
			linesRight.add(rightLines.size());
			countActionsSeed.add((int) (actionsSeed.left().size() + actionsSeed.right().stream().filter(a -> a.type() == Action.Type.ADDED).count()));
			countActionsLcs.add((int) (actionsLcs.left().size() + actionsLcs.right().stream().filter(a -> a.type() == Action.Type.ADDED).count()));

			boolean wasWhiteSeed = false;
			boolean wasWhiteLcs = false;
			int countSeed = 0;
			int countLcs = 0;
			for (int l = 0; l < leftLines.size(); l++) {
				Action actionSeed = actionsSeed.left().get(l);
				Action actionLcs = actionsLcs.left().get(l);
				if (actionSeed.type() == Action.Type.UNCHANGED || actionSeed.type() == Action.Type.UPDATED) {
					if (!wasWhiteSeed) {
						countSeed++;
					}
					wasWhiteSeed = true;
				} else {
					wasWhiteSeed = false;
				}
				if (actionLcs.type() == Action.Type.UNCHANGED || actionLcs.type() == Action.Type.UPDATED) {
					if (!wasWhiteLcs) {
						countLcs++;
					}
					wasWhiteLcs = true;
				} else {
					wasWhiteLcs = false;
				}
			}
			blocksSeed.add(countSeed);
			blocksLcs.add(countLcs);
			System.out.printf(" %d %d%n", countSeed, countLcs);
		}

		CSVBuilder csvBuilder = new CSVBuilder("directory", "lines_left", "lines_right", "actions_seed", "actions_lcs", "blocks_seed", "blocks_lcs");
		for (int i = 0; i < blocksSeed.size(); i++) {
			csvBuilder.add(datasetPath.relativize(directories.get(i)).toString(), linesLeft.get(i), linesRight.get(i),
					countActionsSeed.get(i), countActionsLcs.get(i), blocksSeed.get(i), blocksLcs.get(i));
		}
		String csv = csvBuilder.build();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("bootstrap50-quantitative.csv"));
			writer.write(csv);
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void computeForDiffer(Path directory, LogDiffer differ, List<Line> leftLines, List<Line> rightLines, CSVBuilder csv, String algorithm) {
		Pair<List<Action>> actions = differ.diff(leftLines, rightLines);
		LogsPanel.insertLinesForParallelScrolling(new Pair<>(leftLines, rightLines), actions);

		boolean wasWhite = false;
		int blocks = 0;
		for (int l = 0; l < leftLines.size(); l++) {
			Action actionSeed = actions.left().get(l);
			if (actionSeed.type() == Action.Type.UNCHANGED || actionSeed.type() == Action.Type.UPDATED) {
				if (!wasWhite) {
					blocks++;
				}
				wasWhite = true;
			} else {
				wasWhite = false;
			}
		}
		int actionsCount = (int) (actions.left().size() + actions.right().stream().filter(a -> a.type() == Action.Type.ADDED).count());
		int indels = (int) (actions.left().stream().filter(a -> a.type() == Action.Type.DELETED).count()
						+ actions.right().stream().filter(a -> a.type() == Action.Type.ADDED).count());
		int upuns = (int) (actions.left().stream().filter(a -> a.type() == Action.Type.UPDATED).count()
				+ actions.left().stream().filter(a -> a.type() == Action.Type.UNCHANGED).count()
				+ actions.left().stream().filter(a -> a.type() == Action.Type.MOVED_UPDATED).count()
				+ actions.left().stream().filter(a -> a.type() == Action.Type.MOVED_UNCHANGED).count());
		csv.add(directory.toString(), algorithm, leftLines.size(), actionsCount, indels, upuns, blocks);

	}
	public static void differenceInQuantityOfBlock2() {
		Options.setup(new Properties());  // defaults options are fine
		Path datasetPath = Path.of("/home/ketheroth/these/cidiff/data/breakages/");

		LogParser parser = LogParser.Type.GITHUB.construct();
		LogDiffer differSeed = LogDiffer.Algorithm.SEED.construct();
		LogDiffer differLcs = LogDiffer.Algorithm.LCS.construct();

		List<Path> directories = collectDirectories(datasetPath);

		CSVBuilder csvBuilder = new CSVBuilder("directory", "algorithm", "lines", "actions", "indels", "upuns", "blocks");
		for (int i = 0; i < directories.size(); i++) {
			Path dir = directories.get(i);
			if (dir.toString().contains("juliac")) {
				// skip juliac because I don't have enough memory to compute the lcs diff
				continue;
			}
			System.out.printf("%d/%d %s%n", i, directories.size(), datasetPath.relativize(dir));
			List<Line> leftLines = parser.parse(dir.resolve("pass.log").toString());
			List<Line> rightLines = parser.parse(dir.resolve("fail.log").toString());
			computeForDiffer(datasetPath.relativize(directories.get(i)), differSeed, new ArrayList<>(leftLines), new ArrayList<>(rightLines), csvBuilder, "seed");
			computeForDiffer(datasetPath.relativize(directories.get(i)), differLcs, new ArrayList<>(leftLines), new ArrayList<>(rightLines), csvBuilder, "lcs");
		}

		String csv = csvBuilder.build();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("bootstrap50-quantitative2.csv"));
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

	public record BiValue<A, B>(A a, B b) {
	}
}
