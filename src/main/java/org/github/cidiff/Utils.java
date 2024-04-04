package org.github.cidiff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public final class Utils {

	private static final String TOKEN_SEPARATORS = "\\s+";

	private Utils() {
	}

	public static String[] split(Line line) {
		return split(line.value());
	}
	public static String[] split(String line) {
		return line.split(TOKEN_SEPARATORS);
	}

	public static int lcsLength(char[] left, char[] right) {
		// https://en.wikipedia.org/wiki/Longest_common_subsequence#Computing_the_length_of_the_LCS
		final int l = left.length;
		final int r = right.length;
		final int[][] c = new int[l + 1][r + 1];
		for (int i = 1; i <= l; i++) {
			for (int j = 1; j <= r; j++) {
				if (left[i - 1] == right[j - 1]) {
					c[i][j] = c[i - 1][j - 1] + 1;
				} else {
					c[i][j] = Math.max(c[i][j - 1], c[i - 1][j]);
				}
			}
		}
		return c[l][r];
	}

	public static List<Line> lcs(final List<Line> left, final List<Line> right, BiFunction<Line, Line, Boolean> isEqual) {
		// Find lengths of two strings
		final int leftSz = left.size();
		final int rightSz = right.size();

		// Check if we can avoid calling algorithmC which involves heap space allocation
		if (leftSz == 0 || rightSz == 0) {
			return List.of();
		}

		// Check if we can save even more space
		if (leftSz < rightSz) {
			return algorithmC(right, left, isEqual);
		}
		return algorithmC(left, right, isEqual);
	}

	/**
	 * Adapted from Apache's commons-text library. The original code is available <a href="https://github.com/apache/commons-text/blob/master/src/main/java/org/apache/commons/text/similarity/LongestCommonSubsequence.java">here</a>.
	 * An implementation of "ALG B" from Hirschberg's CACM '71 paper.
	 * Assuming the first input sequence is of size <code>m</code> and the second input sequence is of size
	 * <code>n</code>, this method returns the last row of the dynamic programming (DP) table when calculating
	 * the LCS of the two sequences in <i>O(m*n)</i> time and <i>O(n)</i> space.
	 * The last element of the returned array, is the size of the LCS of the two input sequences.
	 *
	 * @param left first input sequence.
	 * @param right second input sequence.
	 * @return last row of the dynamic-programming (DP) table for calculating the LCS of <code>left</code> and <code>right</code>
	 * @since 1.10.0
	 */
	private static int[] algorithmB(final List<Line> left, final List<Line> right, BiFunction<Line, Line, Boolean> isEqual) {
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
	 * Adapted from Apache's commons-text library. The original code is available <a href="https://github.com/apache/commons-text/blob/master/src/main/java/org/apache/commons/text/similarity/LongestCommonSubsequence.java">here</a>.
	 * An implementation of "ALG C" from Hirschberg's CACM '71 paper.
	 * Assuming the first input sequence is of size <code>m</code> and the second input sequence is of size
	 * <code>n</code>, this method returns the Longest Common Subsequence (LCS) of the two sequences in
	 * <i>O(m*n)</i> time and <i>O(m+n)</i> space.
	 *
	 * @param left  first input sequence.
	 * @param right second input sequence.
	 * @return the LCS of <code>left</code> and <code>right</code>
	 * @since 1.10.0
	 */
	private static List<Line> algorithmC(final List<Line> left, final List<Line> right, BiFunction<Line, Line, Boolean> isEqual) {
		final int m = left.size();
		final int n = right.size();

		final List<Line> out = new ArrayList<>();

		if (m == 1) { // Handle trivial cases, as per the paper
			final Line leftCh = left.get(0);
			for (int j = 0; j < n; j++) {
				if (isEqual.apply(leftCh, right.get(j))) {
					out.add(leftCh);
					break;
				}
			}
		} else if (n > 0 && m > 1) {
			final int mid = m / 2; // Find the middle point

			final List<Line> leftFirstPart = left.subList(0, mid);
			final List<Line> leftSecondPart = left.subList(mid, m);

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
			out.addAll(algorithmC(leftFirstPart, right.subList(0, k), isEqual));
			out.addAll(algorithmC(leftSecondPart, right.subList(k, n), isEqual));
		}

		return out;
	}

	private static List<Line> reverse(final List<Line> s) {
		ArrayList<Line> r = new ArrayList<>();
		for (int i = s.size()-1; i >= 0; --i) {
			r.add(s.get(i));
		}
		return r;
	}

}
