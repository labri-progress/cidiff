package org.cidiff.cidiff.differs;

import org.cidiff.cidiff.Action;
import org.cidiff.cidiff.Line;
import org.cidiff.cidiff.LogDiffer;
import org.cidiff.cidiff.Options;
import org.cidiff.cidiff.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public final class LcsLogDiffer implements LogDiffer {

	/**
	 * Returns the longest common subsequence between two strings.
	 *
	 * @return a list of size 2 int arrays that corresponds
	 * to match of index in left sequence to index in right sequence.
	 */
	static List<int[]> longestCommonSubsequence(List<Line> left, List<Line> right) {
		final int[][] lengths = new int[left.size() + 1][right.size() + 1];
		for (int i = 0; i < left.size(); i++)
			for (int j = 0; j < right.size(); j++)
				if (Options.metric().sim(left.get(i), right.get(j)) >= Options.getRewriteMin())
					lengths[i + 1][j + 1] = lengths[i][j] + 1;
				else
					lengths[i + 1][j + 1] = Math.max(lengths[i + 1][j], lengths[i][j + 1]);

		return extractIndexes(lengths, left.size(), right.size());
	}

	public static List<int[]> extractIndexes(int[][] lengths, int leftSize, int rightSize) {
		final List<int[]> indexes = new ArrayList<>();

		for (int x = leftSize, y = rightSize; x != 0 && y != 0; ) {
			if (lengths[x][y] == lengths[x - 1][y]) x--;
			else if (lengths[x][y] == lengths[x][y - 1]) y--;
			else {
				indexes.add(new int[]{x - 1, y - 1});
				x--;
				y--;
			}
		}
		Collections.reverse(indexes);
		return indexes;
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

		// Identify unchanged lines
		final List<int[]> lcs = longestCommonSubsequence(leftLines, rightLines);
		for (int[] match : lcs) {
			Line leftLine = leftLines.get(match[0]);
			Line rightLine = rightLines.get(match[1]);
			Action action;
			if (leftLine.hasSameValue(rightLine)) {
				action = Action.unchanged(leftLine, rightLine, 1);
			} else {
				action = Action.updated(leftLine, rightLine, Options.metric().sim(leftLine, rightLine));
			}
			leftActions.set(match[0], action);
			rightActions.set(match[1], action);
		}

		// Identify deleted lines
		IntStream.range(0, leftLines.size())
				.filter(i -> leftActions.get(i).isEmpty())
				.forEach(i -> leftActions.set(i, Action.deleted(leftLines.get(i))));

		// Identify added lines
		IntStream.range(0, rightLines.size())
				.filter(i -> rightActions.get(i).isEmpty())
				.forEach(i -> rightActions.set(i, Action.added(rightLines.get(i))));

		return new Pair<>(leftActions, rightActions);
	}
}
