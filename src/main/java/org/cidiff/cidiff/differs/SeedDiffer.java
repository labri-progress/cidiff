package org.cidiff.cidiff.differs;


import org.cidiff.cidiff.Action;
import org.cidiff.cidiff.Line;
import org.cidiff.cidiff.LogDiffer;
import org.cidiff.cidiff.Metric;
import org.cidiff.cidiff.Options;
import org.cidiff.cidiff.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

public class SeedDiffer implements LogDiffer {

	public static final int PRIME = 31;
	public final double rewriteMin;

	public SeedDiffer() {
		rewriteMin = Options.getRewriteMin();
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

	public static Map<Integer, List<Integer>> simpleHash(List<Line> lines) {
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

	public static List<Seed> mergeSeeds(List<Seed> seeds) {
		// complexity: O(n(n+1)/2), where n is proportional to the size of the list (worst case, no merge happen)
		// complexity: Omega(n), where n is proportional to the size of the list (best case, all seeds are merged in one)
		List<Seed> merged = new ArrayList<>();
		Queue<Seed> remaining = new ArrayDeque<>(seeds.stream().sorted().toList());
		while (!remaining.isEmpty()) {
			Seed current = remaining.poll();
			for (Seed test : remaining) {
				if (current.left + current.size == test.left && current.right + current.size == test.right) {
					// current bottom touch test top
					current.size += test.size;
					remaining.remove(test);
				}
				// we should check if the current seed should merge with seeds from the top
				// but because the seeds are sorted, we never reach such case, so no check needed
			}
			merged.add(current);
		}
		return merged;
	}

	public static void extendsSeed(Seed seed, boolean[] leftHasSeed, boolean[] rightHasSeed, List<Line> leftLines, List<Line> rightLines, double rewriteMin) {
		while (seed.left > 0 && seed.right > 0
				&& !leftHasSeed[seed.left - 1] && !rightHasSeed[seed.right - 1]
				&& Options.metric().sim(leftLines.get(seed.left - 1), rightLines.get(seed.right - 1)) >= rewriteMin) {
			seed.extendsUp();
		}
		while (seed.left + seed.size < leftLines.size() && seed.right + seed.size < rightLines.size()
				&& !leftHasSeed[seed.left + seed.size] && !rightHasSeed[seed.right + seed.size]
				&& Options.metric().sim(leftLines.get(seed.left + seed.size), rightLines.get(seed.right + seed.size)) >= rewriteMin) {
			seed.extendsDown();
		}
	}

	public static List<Seed> reduceSeeds(List<Seed> seeds) {
		List<Seed> selected = new ArrayList<>();
		// sort seeds by their size, biggest first, smallest last, then by the smallest left index, then the smallest right index
//		// ie biggest seeds first, then by order of appearance in the log
//		Comparator<Seed> sorter = (seed, other) -> seed.size == other.size ? seed.left == other.left ? other.right - seed.right : other.left - seed.left : other.size - seed.size;
		// sort seeds by their size, biggest first, smallest last, then by the smallest distance between their side
		Comparator<Seed> sorter = (s1, s2) -> s1.size == s2.size ? Math.abs(s1.right - s1.left) - Math.abs(s2.right - s2.left) : s2.size - s1.size;
		List<Seed> remaining = new ArrayList<>(seeds);
		remaining.sort(sorter);

		while (!remaining.isEmpty()) {
			Seed current = remaining.remove(0);
			for (Seed test : remaining) {
				// resize both sides of the seed
				resizeSeed(current.left, current.left + current.size - 1, test.left, test);
				resizeSeed(current.right, current.right + current.size - 1, test.right, test);
			}
//			remaining.removeIf(seed -> seed.size <= 0);  // not supposed to happen
			remaining.sort(sorter);
			selected.add(current);
		}

		return selected;
	}

	public static void resizeSeed(int currentStart, int currentEnd, int testStart, Seed test) {
		int start = Math.max(currentStart, testStart);
		int end = Math.min(currentEnd, testStart + test.size - 1);
		if (start <= end) {
			int size = end - start + 1;
			// intersection has values
			if (start == testStart) {
				//  cl----cs        inter=[tl,cs]
				//      tl----ts
				// shrink the start of the test seed
				test.left += size;
				test.right += size;
				test.size -= size;
			} else if (end == testStart + test.size - 1) {
				//  tl----ts      inter=[cs,tl] for inter valid
				//      cl----cs
				// shrink the end of the test seed
				test.size -= size;
			}
			// the case where the intersection is the current seed never happens because the seeds are sorted by
			// the biggest size first, so it's impossible to have current.size < test.size
			//     cl----cs        inter=[cl,cs]
			//  tl----------ts
		}
		// when the intersection is empty, the test seed is not shrunk
	}

	public static void updateCache(List<Seed> seeds, boolean[] leftHasSeed, boolean[] rightHasSeed) {
		for (Seed seed : seeds) {
			for (int i = 0; i < seed.size; i++) {
				leftHasSeed[seed.left + i] = true;
			}
			for (int i = 0; i < seed.size; i++) {
				rightHasSeed[seed.right + i] = true;
			}
		}
	}

	public List<Seed> backbone(List<Line> leftLines, List<Line> rightLines) {
		Map<Integer, List<Integer>> leftHashes = simpleHash(leftLines);  // hash -> List<line_index>
		Map<Integer, List<Integer>> rightHashes = simpleHash(rightLines);
		List<Seed> seeds = new ArrayList<>();
		boolean[] leftHasSeed = new boolean[leftLines.size()];
		boolean[] rightHasSeed = new boolean[rightLines.size()];
		// step 1: setup 100% certainty seed: a line on the left is unique appears only once on the right
		Iterator<Map.Entry<Integer, List<Integer>>> iterator = leftHashes.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, List<Integer>> entry = iterator.next();
			if (!Options.getEvenIdentical()) {
				// search seeds by unique identical lines
				if (entry.getValue().size() == 1 && rightHashes.containsKey(entry.getKey()) && rightHashes.get(entry.getKey()).size() == 1) {
					int i = entry.getValue().get(0);
					int j = rightHashes.get(entry.getKey()).get(0);
					seeds.add(new Seed(i, j, 1));
					leftHasSeed[i] = true;
					rightHasSeed[j] = true;
					iterator.remove();
	//				rightHashes.remove(entry.getKey());  // it is useless to remove the key-value pair as this map is never iterated on
				}
			} else if (rightHashes.containsKey(entry.getKey()) && rightHashes.get(entry.getKey()).size() == entry.getValue().size()){
				// search seeds by identical lines appearing the same amount of time in both logs
				List<Integer> l = entry.getValue();
				List<Integer> r = rightHashes.get(entry.getKey());
				for (int i = 0; i < l.size(); i++) {
					seeds.add(new Seed(l.get(i), r.get(i), 1));
					leftHasSeed[l.get(i)] = true;
					rightHasSeed[r.get(i)] = true;
				}
				iterator.remove();
			}
		}
		// step 1.5: (optional) merge unique seeds to produce bigger seeds
		if (Options.getMergeAdjacentLInes()) {
			seeds = mergeSeeds(seeds);
		}
		// step 2: extends unique seeds without overlapping on other unique lines and merge seeds if touching
		for (Seed seed : seeds) {
			extendsSeed(seed, leftHasSeed, rightHasSeed, leftLines, rightLines, rewriteMin);
		}
		// step 3: remove overlaps between seeds
		seeds = reduceSeeds(seeds);

		if (Options.getRecursiveSearch()) {
			updateCache(seeds, leftHasSeed, rightHasSeed);
			// step 2.5: (optional) merge touching seeds again
			if (Options.getMergeAdjacentLInes()) {
				seeds = mergeSeeds(seeds);
			}
			// step 3: search all remaining identical (not forced to be unique) and create seeds with them
			List<Seed> newSeeds = new ArrayList<>();
			for (Map.Entry<Integer, List<Integer>> entry : leftHashes.entrySet()) {
				if (rightHashes.containsKey(entry.getKey())) {
					for (Integer leftIndex : entry.getValue()) {
						if (!leftHasSeed[leftIndex]) {
							for (Integer rightIndex : rightHashes.get(entry.getKey())) {
								if (!rightHasSeed[rightIndex]) {
									newSeeds.add(new Seed(leftIndex, rightIndex, 1));
								}
							}
						}
					}
				}
			}
			if (Options.getMergeAdjacentLInes()) {
				newSeeds = mergeSeeds(newSeeds);
			}
			for (Seed seed : newSeeds) {
				extendsSeed(seed, leftHasSeed, rightHasSeed, leftLines, rightLines, rewriteMin);
			}
			newSeeds = reduceSeeds(newSeeds);
			seeds.addAll(newSeeds);
		}
		// step 4: merge two seeds if 1 add/del
//		Iterator<Seed> seedIterator = seeds.iterator();
//		while (seedIterator.hasNext()) {
//			Seed seed = seedIterator.next();
//			seeds.stream().filter(s -> s.left == seed.left + seed.size + 1
//					&& s.right == seed.right + seed.size + 1
//			).findFirst().ifPresent(other -> {
////                System.out.println("found " + seed + " " + other);
//				other.left = seed.left;
//				other.right = seed.right;
//				other.size += seed.size + 1;
//				seedIterator.remove();
//			});
//		}
//		System.out.println("seeds: " + Arrays.deepToString(seeds.stream().sorted().toArray()));
		return seeds;
	}

	@Override
	public Pair<List<Action>> diff(List<Line> leftLines, List<Line> rightLines) {
		Action[] leftActions = new Action[leftLines.size()];
		Arrays.fill(leftActions, Action.EMPTY);
		Action[] rightActions = new Action[rightLines.size()];
		Arrays.fill(rightActions, Action.EMPTY);

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
					action = Action.updated(left, right, Options.metric().sim(left, right));
				}
				leftActions[seed.left + i] = action;
				rightActions[seed.right + i] = action;
			}
		}
		// Identify deleted lines
		IntStream.range(0, leftLines.size())
				.filter(i -> leftActions[i].isEmpty())
				.forEach(i -> leftActions[i] = Action.deleted(leftLines.get(i)));

		// Identify added lines
		IntStream.range(0, rightLines.size())
				.filter(i -> rightActions[i].isEmpty())
				.forEach(i -> rightActions[i] = Action.added(rightLines.get(i)));

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
		List<int[]> lcs = lcs(l, r, (i, j) -> leftActions[i - 1].right().index() == j);
		for (int i = 0; i < leftActions.length; i++) {
			Action action = leftActions[i];
//            Action action = entry.getValue();
			if (action.type() == Action.Type.ADDED || action.type() == Action.Type.DELETED) {
				continue;
			}
			if (lcs.stream().noneMatch(ints -> action.left().index() == l.get(ints[0]))) {
				leftActions[i] = Action.moved(action);
			}
		}
		for (int j = 0; j < rightActions.length; j++) {
			Action action = rightActions[j];
			if (action.type() == Action.Type.ADDED || action.type() == Action.Type.DELETED) {
				continue;
			}
			if (lcs.stream().noneMatch(ints -> action.right().index() == r.get(ints[1]))) {
				rightActions[j] = Action.moved(action);
			}
		}

		return new Pair<>(new ArrayList<>(Arrays.asList(leftActions)), new ArrayList<>(Arrays.asList(rightActions)));
	}

	public static final class Seed implements Comparable<Seed> {

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

		private void extendsUp() {
			left--;
			right--;
			size++;
		}

		private void extendsDown() {
			size++;
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

		@Override
		public int compareTo(Seed other) {
			if (this.left == other.left) {
				if (this.right == other.right) {
					return this.size - other.size;
				} else {
					return this.right - other.right;
				}
			} else {
				return this.left - other.left;
			}
		}
	}

}
