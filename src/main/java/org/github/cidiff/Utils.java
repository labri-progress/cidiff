package org.github.cidiff;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.github.cidiff.Action.Type;

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
			return "" + this.index();
		}

	}

	/**
	 * Allign unchanged and updated lines by inserting empty lines around
	 * added/deleted/moved lines
	 *
	 * @param lines   the lines to align
	 * @param actions the actions of the diff
	 * @return the alligned lines and actions
	 */
	public static Pair.Free<Pair<List<Line>>, Pair<List<Action>>> allignLines(
			Pair<List<Line>> lines, Pair<List<Action>> actions) {
		List<Line> left = new ArrayList<>();
		List<Line> right = new ArrayList<>();
		List<Action> rActions = new ArrayList<>();
		List<Action> lActions = new ArrayList<>();

		int i = 0;
		int j = 0;
		List<Function<Integer, Boolean>> leftConditions = new ArrayList<>();
		List<Function<Integer, Boolean>> rightConditions = new ArrayList<>();

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
					Line pad = new PaddingLine(j);
					right.add(pad);
					Action action = new Action(leftLine, pad, Type.DELETED);
					lActions.add(action);
					rActions.add(action);
					int local = j;
					rightConditions.add((n) -> n >= local);
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
					Line pad = new PaddingLine(j);
					right.add(pad);
					Action action = new Action(leftLine, pad, leftAction.type());
					lActions.add(action);
					rActions.add(action);
					int local = j;
					rightConditions.add((n) -> n >= local);
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
					Line pad = new PaddingLine(i);
					left.add(pad);
					right.add(rightLine);
					Action action = new Action(pad, rightLine, Type.ADDED);
					lActions.add(action);
					rActions.add(action);
					int local = i;
					leftConditions.add((n) -> n >= local);
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
					Line pad = new PaddingLine(i);
					left.add(pad);
					right.add(rightLine);
					Action action = new Action(pad, rightLine, rightAction.type());
					lActions.add(action);
					rActions.add(action);
					int local = i;
					leftConditions.add((n) -> n >= local);
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
		// lines are aligned now but we still need to fix
		// 1. the moved lines are linked to their padding lines and not their real line
		// 2. all the lines have the wrong id in their field
		actions.left().stream()
				.filter(action -> action.type().isIn(Type.MOVED_UPDATED, Type.MOVED_UNCHANGED))
				.forEach(action -> {
					// remap the moved lines together
					int ii = action.left().index();
					for (Function<Integer, Boolean> condition : leftConditions) {
						if (condition.apply(action.left().index())) {
							ii++;
						}
					}
					int jj = action.right().index();
					for (Function<Integer, Boolean> condition : rightConditions) {
						if (condition.apply(action.right().index())) {
							jj++;
						}
					}
					Action move = new Action(left.get(ii), right.get(jj), action.type());
					lActions.set(ii, move);
					rActions.set(jj, move);
				});
		for (i = 0; i < left.size(); ++i) {
			left.get(i).setIndex(i);
		}
		for (j = 0; j < right.size(); ++j) {
			right.get(j).setIndex(j);
		}
		return Pair.Free.of(Pair.of(left, right), Pair.of(lActions, rActions));
	}

}
