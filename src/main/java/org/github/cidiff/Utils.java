package org.github.cidiff;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class Utils {

	private static final Pattern TOKEN_SEPARATORS = Pattern.compile("\\s+");

	private Utils() {
	}

	public static String[] split(Line line) {
		return split(line.value());
	}

	public static String[] split(String line) {
		return TOKEN_SEPARATORS.split(line);
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

	private static class PaddingLine extends Line {

		private PaddingLine(int index) {
			super(index, "");
		}

		@Override
		public String displayValue() {
			return " ";
		}

	}

	/**
	 * Allign unchanged and updated lines by inserting empty lines around insertion/deletion
	 *
	 * @param lines the lines to align
	 * @param actions the actions of the diff
	 */
	public static void allignLines(Pair<List<Line>> lines, Pair<List<Action>> actions) {
		List<Line> left = lines.left();
		List<Line> right = lines.right();
		List<int[]> lcs = new ArrayList<>();  // lines in the lcs are the lines we want to allign
		for (int i = 0; i < left.size(); i++) {
			Action action = actions.left().get(i);
			if (action.type() == Action.Type.UPDATED || action.type() == Action.Type.UNCHANGED) {
				lcs.add(new int[]{action.left().index(), action.right().index()});
			}
		}
		int i = 0;
		int I = 0;
		// lines from 0 to last element in the lcs
		while (I < lcs.size()) {
			int[] match = lcs.get(I);
			while (i < left.size() && i < right.size() && (left.get(i).index() < match[0] || right.get(i).index() < match[1])) {
				insertLine(actions, left, right, i);
				i++;
			}
			i++;
			I++;
		}
		// lines after the lcs
		while (i < left.size() && i < right.size()) {
			insertLine(actions, left, right, i);
			i++;
		}
		// at this point either i >= |left| or i >= |right| or both are higher or equal
		// this means only one of these two for loop will be executed
		for (int j = i; j < left.size(); j++) {
			Action oldLeft = actions.left().get(j);
			if (oldLeft.type() == Action.Type.MOVED_UNCHANGED || oldLeft.type() == Action.Type.MOVED_UPDATED) {
				Line empty = new PaddingLine(right.size());
				right.add(empty);
				actions.right().add(new Action(oldLeft.left(), empty, Action.Type.NONE));
			} else if (oldLeft.type() == Action.Type.DELETED) {
				Line empty = new PaddingLine(right.size());
				right.add(empty);
				Action newAction = new Action(oldLeft.left(), empty, oldLeft.type());
				actions.left().set(j, newAction);
				actions.right().add(newAction);
			}
		}
		for (int j = i; j < right.size(); j++) {
			Action oldRight = actions.right().get(j);
			if (oldRight.type() == Action.Type.MOVED_UNCHANGED || oldRight.type() == Action.Type.MOVED_UPDATED) {
				Line empty = new PaddingLine(left.size());
				left.add(empty);
				actions.left().add(new Action(empty, oldRight.right(), Action.Type.NONE));
			} else if (oldRight.type() == Action.Type.ADDED) {
				Line empty = new PaddingLine(left.size());
				left.add(empty);
				Action newAction = new Action(empty, oldRight.right(), oldRight.type());
				actions.left().add(newAction);
				actions.right().set(j, newAction);
			}
		}

	}

	private static void insertLine(Pair<List<Action>> actions, List<Line> left, List<Line> right, int i) {
		Action oldLeft = actions.left().get(i);
		Action oldRight = actions.right().get(i);
		if (oldLeft.type() == Action.Type.DELETED && oldRight.type() == Action.Type.ADDED) {
			return;
		}
		if (oldLeft.type() == Action.Type.DELETED) {
			Line empty = new PaddingLine(right.size());
			right.add(i, empty);
			// remap the deleted line action to link to the padding line
			Action newAction = new Action(oldLeft.left(), empty, oldLeft.type());
			actions.left().set(i, newAction);
			actions.right().add(i, newAction);
		} else if (oldLeft.type() == Action.Type.MOVED_UPDATED || oldLeft.type() == Action.Type.MOVED_UNCHANGED) {
			Line empty = new PaddingLine(right.size());
			right.add(i, empty);
			// create a new action linking the padding line to the moved line
			// but preserve the old action to keep the moved lines link
			Action newAction = new Action(oldLeft.left(), empty, Action.Type.NONE);
			actions.right().add(i, newAction);
		} else if (oldRight.type() == Action.Type.ADDED) {
			Line empty = new PaddingLine(left.size());
			left.add(i, empty);
			// remap the added line action to link to the padding line
			Action newAction = new Action(empty, oldRight.right(), oldRight.type());
			actions.left().add(i, newAction);
			actions.right().set(i, newAction);
		} else if (oldRight.type() == Action.Type.MOVED_UPDATED || oldRight.type() == Action.Type.MOVED_UNCHANGED) {
			Line empty = new PaddingLine(right.size());
			left.add(i, empty);
			// create a new action linking the padding line to the moved line
			// but preserve the old action to keep the moved lines link
			Action newAction = new Action(empty, oldRight.right(), Action.Type.NONE);
			actions.left().add(i, newAction);
		}
	}

}
