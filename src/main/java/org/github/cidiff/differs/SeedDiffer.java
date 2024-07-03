package org.github.cidiff.differs;

import org.github.cidiff.Action;
import org.github.cidiff.LCS;
import org.github.cidiff.Line;
import org.github.cidiff.LogDiffer;
import org.github.cidiff.Metric;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.IntStream;

public class SeedDiffer implements LogDiffer {

	public enum Variant {
		/**
		 * Create a seed if only the hash is present once and only once in both logs.
		 */
		UNIQUE,
		/**
		 * Create multiple seeds if the hash is present the same amount in both logs.
		 */
		EVEN,
		/**
		 * Create multiple seeds if the hash is present multiple times by taking the lines in order.
		 * Example: left = [a, b, c] and right = [d, e] the seeds will be (a,d) and (b,e). the last line, b, is alone and do not form a seed
		 */
		UNEVEN,
		/**
		 * Create multiple seeds the hash is present in both logs by doing the cartesian product of its appearance.
		 * Example: left = [a, b, c] and right = [d, e] the seeds will be (a,d), (a, e), (b,d), (b,e), (c,d), and (c,e).
		 */
		CARTESIAN
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
			Iterator<Seed> iter = remaining.iterator();
			while (iter.hasNext()) {
				Seed test = iter.next();
				if (current.left + current.size == test.left && current.right + current.size == test.right) {
					// current bottom touch test top
					current.size += test.size;
					iter.remove();
				}
				// we should check if the current seed should merge with seeds from the top
				// but because the seeds are sorted, we never reach such case, so no check needed
			}
			merged.add(current);
		}
		return merged;
	}

	public static void extendsSeed(Seed seed, boolean[] leftHasSeed, boolean[] rightHasSeed, List<Line> leftLines, List<Line> rightLines, Metric metric, double rewriteMin) {
		while (seed.left > 0 && seed.right > 0
				&& !leftHasSeed[seed.left - 1] && !rightHasSeed[seed.right - 1]
				&& metric.sim(leftLines.get(seed.left - 1).value(), rightLines.get(seed.right - 1).value()) >= rewriteMin) {
			seed.extendsUp();
		}
		while (seed.left + seed.size < leftLines.size() && seed.right + seed.size < rightLines.size()
				&& !leftHasSeed[seed.left + seed.size] && !rightHasSeed[seed.right + seed.size]
				&& metric.sim(leftLines.get(seed.left + seed.size).value(), rightLines.get(seed.right + seed.size).value()) >= rewriteMin) {
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

	public List<Seed> backbone(List<Line> leftLines, List<Line> rightLines, Options options) {
		Map<Integer, List<Integer>> leftHashes = simpleHash(leftLines);  // hash -> List<line_index>
		Map<Integer, List<Integer>> rightHashes = simpleHash(rightLines);
		List<Seed> seeds = new ArrayList<>();
		boolean[] leftHasSeed = new boolean[leftLines.size()];
		boolean[] rightHasSeed = new boolean[rightLines.size()];
		// step 1: setup 100% certainty seed: a line on the left is unique appears only once on the right
		Iterator<Map.Entry<Integer, List<Integer>>> iterator = leftHashes.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, List<Integer>> entry = iterator.next();
			if (!rightHashes.containsKey(entry.getKey())) {
				continue;
			}
			switch (options.seedVariant()) {
				case UNIQUE:
					if (entry.getValue().size() == 1 && rightHashes.get(entry.getKey()).size() == 1) {
						int i = entry.getValue().get(0);
						int j = rightHashes.get(entry.getKey()).get(0);
						seeds.add(new Seed(i, j, 1));
						leftHasSeed[i] = true;
						rightHasSeed[j] = true;
					}
					break;
				case EVEN:
					if (rightHashes.get(entry.getKey()).size() == entry.getValue().size()) {
						List<Integer> l = entry.getValue();
						List<Integer> r = rightHashes.get(entry.getKey());
						for (int i = 0; i < l.size(); i++) {
							seeds.add(new Seed(l.get(i), r.get(i), 1));
							leftHasSeed[l.get(i)] = true;
							rightHasSeed[r.get(i)] = true;
						}
					}
					break;
				case UNEVEN: {
					List<Integer> l = entry.getValue();
					List<Integer> r = rightHashes.get(entry.getKey());
					for (int i = 0; i < l.size() && i < r.size(); i++) {
						seeds.add(new Seed(l.get(i), r.get(i), 1));
						leftHasSeed[l.get(i)] = true;
						rightHasSeed[r.get(i)] = true;
					}
				}
				break;
				case CARTESIAN: {
					for (Integer i : entry.getValue()) {
						for (Integer j : rightHashes.get(entry.getKey())) {
							seeds.add(new Seed(i, j, 1));
							leftHasSeed[i] = true;
							rightHasSeed[j] = true;
						}
					}
				}
				break;
			}
			iterator.remove();
		}
		// step 1.5: (optional) merge unique seeds to produce bigger seeds
		if (options.mergeAdjacentLines()) {
			seeds = mergeSeeds(seeds);
		}
		// step 2: extends unique seeds without overlapping on other unique lines and merge seeds if touching
		for (Seed seed : seeds) {
			extendsSeed(seed, leftHasSeed, rightHasSeed, leftLines, rightLines, options.metric(), options.rewriteMin());
		}
		// step 3: remove overlaps between seeds
		seeds = reduceSeeds(seeds);

		if (options.recursiveSearch()) {
			updateCache(seeds, leftHasSeed, rightHasSeed);
			// step 2.5: (optional) merge touching seeds again
			if (options.mergeAdjacentLines()) {
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
			if (options.mergeAdjacentLines()) {
				newSeeds = mergeSeeds(newSeeds);
			}
			for (Seed seed : newSeeds) {
				extendsSeed(seed, leftHasSeed, rightHasSeed, leftLines, rightLines, options.metric(), options.rewriteMin());
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
	public Pair<List<Action>> diff(List<Line> leftLines, List<Line> rightLines, Options options) {
		Action[] leftActions = new Action[leftLines.size()];
		Arrays.fill(leftActions, Action.NONE);
		Action[] rightActions = new Action[rightLines.size()];
		Arrays.fill(rightActions, Action.NONE);

		List<Seed> selected = backbone(leftLines, rightLines, options);

		// use the seeds to produce unchanged actions
		for (Seed seed : selected) {
			for (int i = 0; i < seed.size; i++) {
				Line left = leftLines.get(seed.left + i);
				Line right = rightLines.get(seed.right + i);
				Action action;
				if (left.hasSameValue(right)) {
					action = Action.unchanged(left, right, 1);
				} else {
					action = Action.updated(left, right, options.metric().sim(left.value(), right.value()));
				}
				leftActions[seed.left + i] = action;
				rightActions[seed.right + i] = action;
			}
		}
		// Identify deleted lines
		IntStream.range(0, leftLines.size())
				.filter(i -> leftActions[i].isNone())
				.forEach(i -> leftActions[i] = Action.deleted(leftLines.get(i)));

		// Identify added lines
		IntStream.range(0, rightLines.size())
				.filter(i -> rightActions[i].isNone())
				.forEach(i -> rightActions[i] = Action.added(rightLines.get(i)));

		// post process, compute an LCS to determine moved lines (in unchanged and updated lines only)
		List<Action> subactions = Arrays.stream(leftActions).filter(action -> action.type().isIn(Action.Type.UNCHANGED, Action.Type.UPDATED)).toList();
		List<Line> l = new ArrayList<>();
		List<Line> r = new ArrayList<>();
		for (Action a : subactions) {
			l.add(a.left());
			r.add(a.right());
		}
		l.sort(Comparator.comparingInt(Line::index));
		r.sort(Comparator.comparingInt(Line::index));
		List<Pair<Line>> lcs = LCS.myers(l, r, (leftLine, rightLine) -> leftActions[leftLine.index()].right().index() == rightLine.index());
		boolean[] inleft = new boolean[leftLines.size()];
		boolean[] inright = new boolean[rightLines.size()];
		for (Pair<Line> pair : lcs) {
			inleft[pair.left().index()] = true;
			inright[pair.right().index()] = true;
		}
		for (int i = 0; i < inleft.length; i++) {
			if (!inleft[i] && leftActions[i].type().isIn(Action.Type.UNCHANGED, Action.Type.UPDATED)) {
				Action moved = Action.moved(leftActions[i]);
				leftActions[moved.left().index()] = moved;
				rightActions[moved.right().index()] = moved;
			}
		}
		for (int i = 0; i < inright.length; i++) {
			if (!inright[i] && rightActions[i].type().isIn(Action.Type.UNCHANGED, Action.Type.UPDATED)) {
				Action moved = Action.moved(rightActions[i]);
				leftActions[moved.left().index()] = moved;
				rightActions[moved.right().index()] = moved;
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
