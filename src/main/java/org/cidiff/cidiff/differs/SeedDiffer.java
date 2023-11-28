package org.cidiff.cidiff.differs;


import org.cidiff.cidiff.Action;
import org.cidiff.cidiff.Line;
import org.cidiff.cidiff.LogDiffer;
import org.cidiff.cidiff.Options;
import org.cidiff.cidiff.Pair;
import org.cidiff.cidiff.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SeedDiffer implements LogDiffer {

	public static final int PRIME = 31;
	public final int windowsSize;
	public final double rewriteMin;


	public SeedDiffer() {
		windowsSize = Options.getInstance().getSeedWindowSize();
		rewriteMin = Options.getInstance().getRewriteMin();
	}

	/**
	 * Determine if a seed contains another seed in at least one side.
	 *
	 * @param a the seed to test if b is inside
	 * @param b the seed to test if it is included in a
	 * @return true is seed b is inside the seed a left or right
	 */
	public static boolean containsOneSide(Seed a, Seed b) {
		return a.left <= b.left && (b.left + b.size) <= (a.left + a.size)
				|| a.right <= b.right && (b.right + b.size) <= (a.right + a.size);
	}

	private static List<int[]> lcs(List<Integer> left, List<Integer> right, BiFunction<Integer, Integer, Boolean> areLinesMatching) {
		final int[][] lengths = new int[left.size() + 1][right.size() + 1];
		for (int i = 0; i < left.size(); i++)
			for (int j = 0; j < right.size(); j++) {
				if (areLinesMatching.apply(left.get(i), right.get(j)))
					lengths[i + 1][j + 1] = lengths[i][j] + 1;
				else
					lengths[i + 1][j + 1] = Math.max(lengths[i + 1][j], lengths[i][j + 1]);
			}

		return LcsLogDiffer.extractIndexes(lengths, left.size(), right.size());
	}

	public Map<Integer, List<Integer>> simpleHash(List<Line> lines) {
		Map<Integer, List<Integer>> hashes = new HashMap<>();
		for (int i = 0; i < lines.size(); i++) {
			int hash = lines.get(i).value().hashCode();
			if (hashes.containsKey(hash)) {
				hashes.get(hash).add(i);
			} else {
				List<Integer> list = new ArrayList<>();
				list.add(i);
				hashes.put(hash, list);
			}
		}
		return hashes;
	}

	public List<Seed> backbone(List<Line> leftLines, List<Line> rightLines) {
		Map<Integer, List<Integer>> leftHashes = simpleHash(leftLines);
		Map<Integer, List<Integer>> rightHashes = simpleHash(rightLines);
		Map<Integer, Integer> uniqueLines = new HashMap<>();
//        List<Seed> seeds = new ArrayList<>();
		// step 1: setup 100% certainty seed: a line on the left is unique appears only once on the right
		for (Map.Entry<Integer, List<Integer>> entry : leftHashes.entrySet()) {
			if (rightHashes.containsKey(entry.getKey())) {
				if (entry.getValue().size() == 1 && rightHashes.get(entry.getKey()).size() == 1) {
					int i = entry.getValue().get(0);
					int j = rightHashes.get(entry.getKey()).get(0);
//                    seeds.add(new Seed(i, j, 1));
					uniqueLines.put(i, j);
				}
			}
		}
		// step 1.5: merge unique lines to produce bigger seeds
		List<Seed> seeds = new ArrayList<>();
		Map<Integer, Seed> leftLineToSeed = new HashMap<>();
		Map<Integer, Seed> rightLineToSeed = new HashMap<>();
		for (Map.Entry<Integer, Integer> entry : uniqueLines.entrySet()) {
			int i = entry.getKey();
			int j = entry.getValue();
			if (!leftLineToSeed.containsKey(i)) {
				while (uniqueLines.containsKey(i + 1) && uniqueLines.get(i + 1) == j + 1) {
					i++;
					j++;
				}
				Seed seed = new Seed(entry.getKey(), entry.getValue(), i - entry.getKey() + 1);
				seeds.add(seed);
				for (int k = 0; k < seed.size; k++) {
					leftLineToSeed.put(seed.left + k, seed);
					rightLineToSeed.put(seed.right + k, seed);
				}
			}
		}
		// step 2: extends unique lines without overlapping on other unique lines and merge seeds if touching
		for (Seed seed : seeds) {
			while (seed.left > 0 && seed.right > 0
					&& !leftLineToSeed.containsKey(seed.left - 1) && !rightLineToSeed.containsKey(seed.right - 1)
					&& Utils.rewriteSim(leftLines.get(seed.left - 1).value(), rightLines.get(seed.right - 1).value()) >= rewriteMin) {
				seed.increaseFromStart();
				leftLineToSeed.put(seed.left, seed);
				rightLineToSeed.put(seed.right, seed);
			}
			while (seed.left + seed.size <= leftLines.size() - 1 && seed.right + seed.size <= rightLines.size() - 1
					&& !leftLineToSeed.containsKey(seed.left + seed.size) && !rightLineToSeed.containsKey(seed.right + seed.size)
					&& Utils.rewriteSim(leftLines.get(seed.left + seed.size).value(), rightLines.get(seed.right + seed.size).value()) >= rewriteMin) {
				seed.increaseFromEnd();
				leftLineToSeed.put(seed.left + seed.size - 1, seed);
				rightLineToSeed.put(seed.right + seed.size - 1, seed);
			}
		}
		// step 2.5: merge touching seeds again
		Iterator<Seed> iterator = seeds.iterator();
		while (iterator.hasNext()) {
			Seed seed = iterator.next();
			seeds.stream()
					.filter(s -> seed.left + seed.size == s.left && seed.right + seed.size == s.right)
					.findAny()
					.ifPresent(touching -> {
						touching.left = seed.left;
						touching.right = seed.right;
						touching.size += seed.size;
						iterator.remove();
					});
		}
		// step 3: find seeds in remaining line (lines not in seeds)
		for (Map.Entry<Integer, List<Integer>> entry : leftHashes.entrySet()) {
			if (!rightHashes.containsKey(entry.getKey()) || entry.getValue().stream().allMatch(leftLineToSeed::containsKey)) {
				continue;
			}
			// hash in both side
			List<Seed> newSeeds = new ArrayList<>();
			for (Integer leftLine : entry.getValue()) {
				if (leftLineToSeed.containsKey(leftLine)) {
					// left line already selected
					continue;
				}
				for (Integer rightLine : rightHashes.get(entry.getKey())) {
					if (rightLineToSeed.containsKey(rightLine)) {
						// right line already selected
						continue;
					}
					// the two lines are equals
					Seed seed = new Seed(leftLine, rightLine, 1);
					while (seed.left > 0 && seed.right > 0
							&& !leftLineToSeed.containsKey(seed.left - 1) && !rightLineToSeed.containsKey(seed.right - 1)
							&& Utils.rewriteSim(leftLines.get(seed.left - 1).value(), rightLines.get(seed.right - 1).value()) >= rewriteMin) {
						seed.increaseFromStart();
					}
					while (seed.left + seed.size <= leftLines.size() - 1 && seed.right + seed.size <= rightLines.size() - 1
							&& !leftLineToSeed.containsKey(seed.left + seed.size) && !rightLineToSeed.containsKey(seed.right + seed.size)
							&& Utils.rewriteSim(leftLines.get(seed.left + seed.size).value(), rightLines.get(seed.right + seed.size).value()) >= rewriteMin) {
						seed.increaseFromEnd();
					}
					newSeeds.add(seed);
				}
			}
			// add the biggest seeds
			newSeeds = newSeeds.stream()
					.distinct()
					.sorted((s1, s2) -> s1.size() == s2.size()
							? Integer.compare(Math.abs(s1.right() - s1.left()), Math.abs(s2.right() - s2.left()))
							: Integer.compare(s2.size(), s1.size()))
					.collect(Collectors.toList());
//            System.out.println("new " + Arrays.deepToString(newSeeds.toArray()));
			while (!newSeeds.isEmpty()) {
				// add the biggest remaining seed
				Seed biggest = newSeeds.get(0);
				for (int i = 0; i < biggest.size; i++) {
					leftLineToSeed.put(biggest.left + i, biggest);
					rightLineToSeed.put(biggest.right + i, biggest);
				}
				seeds.add(biggest);
				newSeeds.remove(biggest);
				// remove remaining seeds inside the biggest seed
				// and reduce the size of overlapping seeds with the biggest
				Iterator<Seed> newSeedsIterator = newSeeds.iterator();
				while (newSeedsIterator.hasNext()) {
					Seed seed = newSeedsIterator.next();
					if (containsOneSide(biggest, seed)) {
						newSeedsIterator.remove();
						continue;
					}

					// reduce seed overlapping by its start
					int biggestEndLeft = biggest.left + biggest.size - 1;
					if (biggest.left <= seed.left && seed.left <= biggestEndLeft) {
						while (seed.left <= biggestEndLeft && seed.size > 0) {
							seed.decreaseFromStart();
						}
					}
					int biggestEndRight = biggest.right + biggest.size - 1;
					if (biggest.right <= seed.right && seed.right <= biggestEndRight) {
						while (seed.right <= biggestEndRight && seed.size > 0) {
							seed.decreaseFromStart();
						}
					}
					// reduce seed overlapping by its end
					if (biggest.left <= seed.left + seed.size - 1 && seed.left + seed.size - 1 <= biggestEndLeft) {
						while (seed.left + seed.size - 1 >= biggest.left) {
							seed.decreaseFromEnd();
						}
					}
					if (biggest.right <= seed.right + seed.size - 1 && seed.right + seed.size - 1 <= biggestEndRight) {
						while (seed.right + seed.size - 1 >= biggest.right) {
							seed.decreaseFromEnd();
						}
					}

					if (seed.size <= 0) {
						newSeedsIterator.remove();
					}
				}
			}
		}
		// step 4: merge two seeds if 1 add/del
		Iterator<Seed> seedIterator = seeds.iterator();
		while (seedIterator.hasNext()) {
			Seed seed = seedIterator.next();
			seeds.stream().filter(s -> s.left == seed.left + seed.size + 1
					&& s.right == seed.right + seed.size + 1
			).findFirst().ifPresent(other -> {
//                System.out.println("found " + seed + " " + other);
				other.left = seed.left;
				other.right = seed.right;
				other.size += seed.size + 1;
				seedIterator.remove();
			});
		}
		return seeds;
	}

	@Override
	public Pair<List<Action>> diff(List<Line> leftLines, List<Line> rightLines) {
		List<Action> leftActions = new ArrayList<>();
		for (int i = 0; i < leftLines.size(); i++) {
			leftActions.add(Action.EMPTY);
		}
		List<Action> rightActions = new ArrayList<>();
		for (int i = 0; i < rightLines.size(); i++) {
			rightActions.add(Action.EMPTY);
		}

		List<Seed> selected = backbone(leftLines, rightLines);

		// use the seeds to produce unchanged actions
		for (Seed seed : selected) {
			for (int i = 0; i < seed.size; i++) {
				Line left = leftLines.get(seed.left + i);
				Line right = rightLines.get(seed.right + i);
				Action action;
				if (left.hasSameValue(right)) {
					action = Action.unchanged(left, right, 1);
				} else {
					action = Action.updated(left, right, Utils.rewriteSim(left, right));
				}
				leftActions.set(seed.left + i, action);
				rightActions.set(seed.right + i, action);
			}
		}
		// Identify deleted lines
		IntStream.range(0, leftLines.size())
				.filter(i -> leftActions.get(i).isEmpty())
				.forEach(i -> leftActions.set(i, Action.deleted(leftLines.get(i))));

		// Identify added lines
		IntStream.range(0, rightLines.size())
				.filter(i -> rightActions.get(i).isEmpty())
				.forEach(i -> rightActions.set(i, Action.added(rightLines.get(i))));

		// post process, compute an LCS to determine moved lines (in unchanged and updated lines only)
		List<Integer> l = new ArrayList<>();
		List<Integer> r = new ArrayList<>();
		for (Action action : leftActions) {
			if (action.type() == Action.Type.UNCHANGED || action.type() == Action.Type.UPDATED) {
				l.add(action.left().index());
				r.add(action.right().index());
			}
		}
		Collections.sort(l);
		Collections.sort(r);
		// lcs values are index of l and r
		// l and r values are Line#index values (1-indexed) of leftLines and rightLines
		List<int[]> lcs = lcs(l, r, (i, j) -> leftActions.get(i - 1).right().index() == j);
		for (int i = 0; i < leftActions.size(); i++) {
			Action action = leftActions.get(i);
//            Action action = entry.getValue();
			if (action.type() == Action.Type.ADDED || action.type() == Action.Type.DELETED) {
				continue;
			}
			if (lcs.stream().noneMatch(ints -> action.left().index() == l.get(ints[0]))) {
				leftActions.set(i, Action.moved(action));
			}
		}
		for (int j = 0; j < rightActions.size(); j++) {
			Action action = rightActions.get(j);
			if (action.type() == Action.Type.ADDED || action.type() == Action.Type.DELETED) {
				continue;
			}
			if (lcs.stream().noneMatch(ints -> action.right().index() == r.get(ints[1]))) {
				rightActions.set(j, Action.moved(action));
			}
		}

		return new Pair<>(leftActions, rightActions);
	}

	public static final class Seed {

		/**
		 * The start of the seed in the left log.
		 */
		private int left;
		/**
		 * The start of the seed in the right log.
		 */
		private int right;
		/**
		 * The size of the seed.
		 */
		private int size;

		public Seed(int left, int right, int size) {
			this.left = left;
			this.right = right;
			this.size = size;
		}

		private void increaseFromStart() {
			left--;
			right--;
			size++;
		}

		private void increaseFromEnd() {
			size++;
		}

		private void decreaseFromStart() {
			left++;
			right++;
			size--;
		}

		private void decreaseFromEnd() {
			size--;
		}

		public int left() {
			return left;
		}

		public int right() {
			return right;
		}

		public int size() {
			return size;
		}

		@Override
		public String toString() {
			return "(%d,%d,%d)".formatted(left, right, size);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) return true;
			if (other == null || getClass() != other.getClass()) return false;
			Seed seed = (Seed) other;
			return this.left == seed.left
					&& this.right == seed.right
					&& this.size == seed.size;

		}

		@Override
		public int hashCode() {
			return Objects.hash(left, right, size);
		}
	}

}
