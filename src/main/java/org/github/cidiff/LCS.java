/*
 * Modified from original work licensed under the Apache License, Version 2.0
 * Original sources:
 *   - Hirschberg's algo: https://github.com/apache/commons-text/blob/master/src/main/java/org/apache/commons/text/similarity/LongestCommonSubsequence.java
 *   - Myers' algo: https://github.com/apache/commons-collections/blob/master/src/main/java/org/apache/commons/collections4/sequence/SequencesComparator.java
 *
 * Modifications:
 *   - Changed the parameters of the functions to take a `List<T>` instead of a `CharSequence`
 *   - Made `buildScript` stateless, thus changed its parameters
 *   - Changed the functions to return a `List<T>` instead of their original type
 */
package org.github.cidiff;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class LCS {

	private LCS() {
	}

	public static <T> List<Pair<T>> hirschberg(final List<T> left, final List<T> right, BiFunction<T, T, Boolean> isEqual) {
		// Find lengths of two strings
		final int leftSz = left.size();
		final int rightSz = right.size();

		// Check if we can avoid calling algorithmC which involves heap space allocation
		if (leftSz == 0 || rightSz == 0) {
			return List.of();
		}

		// Check if we can save even more space
		if (leftSz < rightSz) {
			return algorithmC(right, left, isEqual, true);
		}
		return algorithmC(left, right, isEqual, false);
	}

	public static <T> List<Pair<T>> myers(final List<T> left, final List<T> right, BiFunction<T, T, Boolean> isEqual) {
		final List<Pair<T>> lcs = new ArrayList<>();
		final int[] vDown = new int[left.size() + right.size() + 2];
		final int[] vUp = new int[left.size() + right.size() + 2];
		buildScript(left, right, 0, left.size(), 0, right.size(), vDown, vUp, lcs, isEqual);
		return lcs;
	}

	/**
	 * An implementation of "ALG B" from Hirschberg's CACM '71 paper.
	 * Assuming the first input sequence is of size <code>m</code> and the second input sequence is of size
	 * <code>n</code>, this method returns the last row of the dynamic programming (DP) table when calculating
	 * the LCS of the two sequences in <i>O(m*n)</i> time and <i>O(n)</i> space.
	 * The last element of the returned array, is the size of the LCS of the two input sequences.
	 * <p>
	 *
	 * @param left    first input sequence.
	 * @param right   second input sequence.
	 * @param isEqual function to determine if two element are equal
	 * @return last row of the dynamic-programming (DP) table for calculating the LCS of <code>left</code> and <code>right</code>
	 */
	private static <T> int[] algorithmB(final List<T> left, final List<T> right, BiFunction<T, T, Boolean> isEqual) {
		final int m = left.size();
		final int n = right.size();

		// Creating an array for storing two rows of DP table
		final int[][] dpRows = new int[2][1 + n];

		for (int i = 1; i <= m; i++) {
			// K(0, j) <- K(1, j) [j = 0...n], as per the paper:
			// Since we have references in Java, we can swap references instead of literal copying.
			// We could also use a "binary index" using modulus operator, but directly swapping the
			// two rows helps readability and keeps the code consistent with the algorithm description
			// in the paper.
			final int[] temp = dpRows[0];
			dpRows[0] = dpRows[1];
			dpRows[1] = temp;

			for (int j = 1; j <= n; j++) {
				if (isEqual.apply(left.get(i - 1), right.get(j - 1))) {
					dpRows[1][j] = dpRows[0][j - 1] + 1;
				} else {
					dpRows[1][j] = Math.max(dpRows[1][j - 1], dpRows[0][j]);
				}
			}
		}

		// LL(j) <- K(1, j) [j=0...n], as per the paper:
		// We don't need literal copying of the array, we can just return the reference
		return dpRows[1];
	}

	/**
	 * An implementation of "ALG C" from Hirschberg's CACM '71 paper.
	 * Assuming the first input sequence is of size <code>m</code> and the second input sequence is of size
	 * <code>n</code>, this method returns the Longest Common Subsequence (LCS) of the two sequences in
	 * <i>O(m*n)</i> time and <i>O(m+n)</i> space.
	 * <p>
	 *
	 * @param left      first input sequence.
	 * @param right     second input sequence.
	 * @param isEqual   function to determine if two element are equal
	 * @param isFlipped determine if left and right parameters were flipped
	 * @return the LCS of <code>left</code> and <code>right</code>
	 */
	private static <T> List<Pair<T>> algorithmC(final List<T> left, final List<T> right, BiFunction<T, T, Boolean> isEqual, boolean isFlipped) {
		final int m = left.size();
		final int n = right.size();

		final List<Pair<T>> out = new ArrayList<>();

		if (m == 1) { // Handle trivial cases, as per the paper
			final T leftCh = left.get(0);
			for (int j = 0; j < n; j++) {
				if (isEqual.apply(leftCh, right.get(j))) {
					if (isFlipped) {
						out.add(new Pair<>(right.get(j), leftCh));
					} else {
						out.add(new Pair<>(leftCh, right.get(j)));
					}
					break;
				}
			}
		} else if (n > 0 && m > 1) {
			final int mid = m / 2; // Find the middle point

			final List<T> leftFirstPart = left.subList(0, mid);
			final List<T> leftSecondPart = left.subList(mid, m);

			// Step 3 of the algorithm: two calls to Algorithm B
			final int[] l1 = algorithmB(leftFirstPart, right, isEqual);
			final int[] l2 = algorithmB(reverse(leftSecondPart), reverse(right), isEqual);

			// Find k, as per the Step 4 of the algorithm
			int k = 0;
			int t = 0;
			for (int j = 0; j <= n; j++) {
				final int s = l1[j] + l2[n - j];
				if (t < s) {
					t = s;
					k = j;
				}
			}

			// Step 5: solve simpler problems, recursively
			out.addAll(algorithmC(leftFirstPart, right.subList(0, k), isEqual, isFlipped));
			out.addAll(algorithmC(leftSecondPart, right.subList(k, n), isEqual, isFlipped));
		}

		return out;
	}

	private static <T> List<T> reverse(final List<T> s) {
		ArrayList<T> r = new ArrayList<>();
		for (int i = s.size() - 1; i >= 0; --i) {
			r.add(s.get(i));
		}
		return r;
	}

	/**
	 * Build an edit script.
	 * <p>
	 *
	 * @param start1 the begin of the first sequence to be compared
	 * @param end1   the end of the first sequence to be compared
	 * @param start2 the begin of the second sequence to be compared
	 * @param end2   the end of the second sequence to be compared
	 * @param script the edited script
	 */
	private static <T> void buildScript(final List<T> left, final List<T> right,
	                                    final int start1, final int end1, final int start2, final int end2,
										final int[] vDown, final int[] vUp,
	                                    final List<Pair<T>> script, BiFunction<T, T, Boolean> equator) {

		final Snake middle = getMiddleSnake(left, right, start1, end1, start2, end2, vDown, vUp, equator);

		if (middle == null
				|| middle.start() == end1 && middle.diag() == end1 - end2
				|| middle.end() == start1 && middle.diag() == start1 - start2) {

			int i = start1;
			int j = start2;
			while (i < end1 || j < end2) {
				if (i < end1 && j < end2 && equator.apply(left.get(i), right.get(j))) {
					script.add(new Pair<>(left.get(i), right.get(j)));
					++i;
					++j;
				} else {
					if (end1 - start1 > end2 - start2) {
//						script.add(new DeleteCommand<>(left.get(i)));
						++i;
					} else {
//						script.add(new InsertCommand<>(right.get(j)));
						++j;
					}
				}
			}

		} else {

			buildScript(left, right, start1, middle.start(),
					start2, middle.start() - middle.diag(), vDown, vUp,
					script, equator);
			int j = middle.start() - middle.diag();
			for (int i = middle.start(); i < middle.end(); ++i) {
				script.add(new Pair<>(left.get(i), right.get(j)));
				++j;
			}
			buildScript(left, right, middle.end(), end1,
					middle.end() - middle.diag(), end2, vDown, vUp,
					script, equator);
		}
	}

	/**
	 * Get the middle snake corresponding to two subsequences of the
	 * main sequences.
	 * <p>
	 * The snake is found using the MYERS Algorithm (this algorithms has
	 * also been implemented in the GNU diff program). This algorithm is
	 * explained in Eugene Myers article:
	 * <a href="http://www.cs.arizona.edu/people/gene/PAPERS/diff.ps">
	 * An O(ND) Difference Algorithm and Its Variations</a>.
	 * <p>
	 *
	 * @param start1 the begin of the first sequence to be compared
	 * @param end1   the end of the first sequence to be compared
	 * @param start2 the begin of the second sequence to be compared
	 * @param end2   the end of the second sequence to be compared
	 * @return the middle snake
	 */
	private static <T> Snake getMiddleSnake(final List<T> left, final List<T> right,
	                                        final int start1, final int end1, final int start2, final int end2,
											final int[] vDown, final int[] vUp,
	                                        BiFunction<T, T, Boolean> equator) {
		// Myers Algorithm
		// Initialisations
		final int m = end1 - start1;
		final int n = end2 - start2;
		if (m == 0 || n == 0) {
			return null;
		}

		final int delta = m - n;
		final int sum = n + m;
		final int offset = (sum % 2 == 0 ? sum : sum + 1) / 2;
		vDown[1 + offset] = start1;
		vUp[1 + offset] = end1 + 1;

		for (int d = 0; d <= offset; ++d) {
			// Down
			for (int k = -d; k <= d; k += 2) {
				// First step

				final int i = k + offset;
				if (k == -d || k != d && vDown[i - 1] < vDown[i + 1]) {
					vDown[i] = vDown[i + 1];
				} else {
					vDown[i] = vDown[i - 1] + 1;
				}

				int x = vDown[i];
				int y = x - start1 + start2 - k;

				while (x < end1 && y < end2 && equator.apply(left.get(x), right.get(y))) {
					vDown[i] = ++x;
					++y;
				}
				// Second step
				if (delta % 2 != 0 && delta - d <= k && k <= delta + d) {
					if (vUp[i - delta] <= vDown[i]) { // NOPMD
						return buildSnake(left, right, vUp[i - delta], k + start1 - start2, end1, end2, equator);
					}
				}
			}

			// Up
			for (int k = delta - d; k <= delta + d; k += 2) {
				// First step
				final int i = k + offset - delta;
				if (k == delta - d
						|| k != delta + d && vUp[i + 1] <= vUp[i - 1]) {
					vUp[i] = vUp[i + 1] - 1;
				} else {
					vUp[i] = vUp[i - 1];
				}

				int x = vUp[i] - 1;
				int y = x - start1 + start2 - k;
				while (x >= start1 && y >= start2
						&& equator.apply(left.get(x), right.get(y))) {
					vUp[i] = x--;
					y--;
				}
				// Second step
				if (delta % 2 == 0 && -d <= k && k <= d) {
					if (vUp[i] <= vDown[i + delta]) { // NOPMD
						return buildSnake(left, right, vUp[i], k + start1 - start2, end1, end2, equator);
					}
				}
			}
		}

		// this should not happen
		throw new RuntimeException("Internal Error");
	}

	/**
	 * Build a snake.
	 * <p>
	 *
	 * @param start the value of the start of the snake
	 * @param diag  the value of the diagonal of the snake
	 * @param end1  the value of the end of the first sequence to be compared
	 * @param end2  the value of the end of the second sequence to be compared
	 * @return the snake built
	 */
	private static <T> Snake buildSnake(final List<T> left, final List<T> right,
	                                    final int start, final int diag, final int end1, final int end2, BiFunction<T, T, Boolean> equator) {
		int end = start;
		while (end - diag < end2
				&& end < end1
				&& equator.apply(left.get(end), right.get(end - diag))) {
			++end;
		}
		return new Snake(start, end, diag);
	}

	/**
	 * This class is a simple placeholder to hold the end part of a path
	 * under construction in a {@link #myers}.
	 * <p>
	 *
	 * @param start Start index.
	 * @param end   End index.
	 * @param diag  Diagonal number.
	 */
	private record Snake(int start, int end, int diag) { }

}
