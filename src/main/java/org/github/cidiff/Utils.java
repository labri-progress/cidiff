package org.github.cidiff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.github.cidiff.Action.Type;

public final class Utils {

	private static HashMap<String, String[]> cache = new HashMap<>();

	private static final Pattern TOKEN_SEPARATORS = Pattern.compile("\\s+");

	private Utils() {
	}

	public static String[] split(Line line) {
		return split(line.value());
	}

	public static String[] split(String line) {
		if (cache.containsKey(line)) {
			return cache.get(line);
		} else {
			String[] split = TOKEN_SEPARATORS.split(line.trim());
			cache.put(line, split);
			return split;
		}
	}

	public static void resetCache() {
		cache.clear();
		cache = new HashMap<>();
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
			return "" + this.index();
		}

	}

	/**
	 * Align unchanged and updated lines by inserting empty lines around
	 * added/deleted/moved lines
	 *
	 * @param lines   the lines to align
	 * @param actions the actions of the diff
	 * @return the aligned lines and actions
	 */
	public static Pair.Free<Pair<List<Line>>, Pair<List<Action>>> alignLines(
			Pair<List<Line>> lines, Pair<List<Action>> actions) {
		List<Line> left = new ArrayList<>();
		List<Line> right = new ArrayList<>();
		List<Action> rActions = new ArrayList<>();
		List<Action> lActions = new ArrayList<>();

		int i = 0;
		int j = 0;

		while (i < lines.left().size() && j < lines.right().size()) {
			Line leftLine = lines.left().get(i);
			Line rightLine = lines.right().get(j);
			Action leftAction = actions.left().get(i);
			Action rightAction = actions.right().get(j);
			if (leftAction.type() == Type.DELETED) {
				if (rightAction.type() == Type.ADDED) {
					// deleted line is parallel to an added line, do not add blank lines
					left.add(leftLine);
					right.add(rightLine);
					// let's do a little trick and link the two indel lines together
					lActions.add(new Action(leftLine, rightLine, Type.DELETED));
					rActions.add(new Action(leftLine, rightLine, Type.ADDED));
					i++;
					j++;
				} else {
					// deleted line is alone, add a blank line
					left.add(leftLine);
					Line pad = new PaddingLine(0);
					pad.setIndex(right.size());
					right.add(pad);
					Action action = new Action(leftLine, pad, Type.DELETED);
					lActions.add(action);
					rActions.add(action);
					i++;
				}
			} else if (leftAction.type() == Type.MOVED_UPDATED || leftAction.type() == Type.MOVED_UNCHANGED) {
				if (leftAction.right().equals(rightLine)) {
					// move is between the two current lines, do not add a blank line
					left.add(leftLine);
					right.add(rightLine);
					lActions.add(leftAction);
					rActions.add(rightAction);
					i++;
					j++;
				} else {
					// moved line is not parallel to itself, add a blank line
					left.add(leftLine);
					Line pad = new PaddingLine(0);
					right.add(pad);
					Action action = new Action(leftLine, pad, leftAction.type());
					lActions.add(leftAction);  // keep the left-right mapping
					rActions.add(action);  // but map the right padding line to the left line
					i++;
				}
			} else if (rightAction.type() == Type.ADDED) {
				if (leftAction.type() == Type.DELETED) {
					// added line is parallel to an deleted line, do not add a blank line
					left.add(leftLine);
					right.add(rightLine);
					// let's do a little trick and link the two indel lines together
					lActions.add(new Action(leftLine, rightLine, Type.DELETED));
					rActions.add(new Action(leftLine, rightLine, Type.ADDED));
					lActions.add(leftAction);
					rActions.add(rightAction);
					i++;
					j++;
				} else {
					// added line is alone, add a blank line
					Line pad = new PaddingLine(0);
					left.add(pad);
					right.add(rightLine);
					Action action = new Action(pad, rightLine, Type.ADDED);
					lActions.add(action);
					rActions.add(action);
					j++;
				}
			} else if (rightAction.type() == Type.MOVED_UNCHANGED || rightAction.type() == Type.MOVED_UPDATED) {
				if (rightAction.left().equals(leftLine)) {
					// moved line is parallel to itself, do not add a blank line
					left.add(leftLine);
					right.add(rightLine);
					lActions.add(leftAction);
					rActions.add(rightAction);
					i++;
					j++;
				} else {
					// moved line is not parallel to itself, add a blank line
					Line pad = new PaddingLine(0);
					left.add(pad);
					right.add(rightLine);
					Action action = new Action(pad, rightLine, rightAction.type());
					lActions.add(action);  // map the right padding line to the left line
					rActions.add(rightAction);  // but keep the left-right mapping
					j++;
				}
			} else {
				// two white lines that are parallel (they should be parallel)
				left.add(leftLine);
				right.add(rightLine);
				lActions.add(leftAction);
				rActions.add(rightAction);
				i++;
				j++;
			}
		}
		// if the logs don't have the same size, there are remaining line in one side, we must not forget them
		while (i < lines.left().size()) {
			Line leftLine = lines.left().get(i);
			Action leftAction = actions.left().get(i);
			Line pad = new PaddingLine(0);
			left.add(leftLine);
			right.add(pad);
			Action action = new Action(leftLine, pad, leftAction.type());  // keep the old action type
			lActions.add(action);
			rActions.add(action);
			i++;
		}
		while (j < lines.right().size()) {
			Line rightLine = lines.right().get(j);
			Action rightAction = actions.right().get(j);
			Line pad = new PaddingLine(0);
			left.add(pad);
			right.add(rightLine);
			Action action = new Action(pad, rightLine, rightAction.type()); // keep the old action type
			lActions.add(action);
			rActions.add(action);
			j++;
		}
		// lines are aligned now but we still need to fix the id of the lines in their field
		for (i = 0; i < left.size(); ++i) {
			left.get(i).setIndex(i);
		}
		for (j = 0; j < right.size(); ++j) {
			right.get(j).setIndex(j);
		}
		return Pair.Free.of(Pair.of(left, right), Pair.of(lActions, rActions));
	}

}
