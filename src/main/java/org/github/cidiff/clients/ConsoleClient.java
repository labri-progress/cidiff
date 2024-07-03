package org.github.cidiff.clients;

import org.github.cidiff.Action;
import org.github.cidiff.Line;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;
import org.github.cidiff.Utils;

import java.util.List;

public final class ConsoleClient extends AbstractDiffClient {

	static final String RED_FONT = "\033[0;31m";
	static final String GREEN_FONT = "\033[0;32m";
	static final String NO_COLOR_FONT = "\033[0m";
	static final String BOLD_FONT = "\033[0;1m";
	static final String REGULAR_FONT = "\033[0m";

	public ConsoleClient(Pair<List<Line>> lines, Pair<List<Action>> actions) {
		super(lines, actions);
	}

	@Override
	public void execute(Options options) {
		int maxLineNumberSize = Integer.toString(Math.max(lines.left().size(), lines.right().size())).length();
		String lineFormat = "%0" + maxLineNumberSize + "d";
		int lastDisplayed = 0;
		List<Line> left = lines.left();
		for (int i = 0; i < left.size(); i++) {
			Line leftLine = left.get(i);
			Action action = actions.left().get(i);

			if (action.type() == Action.Type.UPDATED) {
				if (options.consoleDisplayUpdated()) {
					boolean newLine = lastDisplayed != 0 && lastDisplayed != leftLine.index();
					if (newLine) {
						System.out.println();
					}

					lastDisplayed = leftLine.index();
					String[] leftTokens = Utils.split(action.left());
					String[] rightTokens = Utils.split(action.right());
					StringBuilder leftValue = new StringBuilder();
					StringBuilder rightValue = new StringBuilder();
					for (int k = 0; k < leftTokens.length; k++) {
						if (leftTokens[k].equals(rightTokens[k])) {
							leftValue.append(leftTokens[k]);
							rightValue.append(rightTokens[k]);
						} else {
							leftValue.append(BOLD_FONT + leftTokens[k] + REGULAR_FONT);
							rightValue.append(BOLD_FONT + rightTokens[k] + REGULAR_FONT);
						}
						leftValue.append(" ");
						rightValue.append(" ");
					}

					String leftLineNumber = String.format(lineFormat, action.left().index());
					String leftOutput = String.format("\t> %s %s", leftLineNumber, leftValue);
					System.out.println(leftOutput);
					String rightLineNumber = String.format(lineFormat, action.right().index());
					String rightOutput = String.format("\t  %s %s", rightLineNumber, rightValue);
					System.out.println(rightOutput);
				}
			} else if (action.type() == Action.Type.UNCHANGED) {
				if (options.consoleDisplayUnchanged()) {
					boolean newLine = lastDisplayed != 0 && lastDisplayed != leftLine.index();
					if (newLine) {
						System.out.println();
					}

					lastDisplayed = leftLine.index();
					String leftLineNumber = String.format(lineFormat, action.left().index());
					String leftOutput = String.format("\t= %s %s", leftLineNumber, action.left().value());
					System.out.println(leftOutput);
					String rightLineNumber = String.format(lineFormat, action.right().index());
					String rightOutput = String.format("\t  %s %s", rightLineNumber, action.right().value());
					System.out.println(rightOutput);
				}
			} else if (action.type() == Action.Type.DELETED) {
				if (options.consoleDisplayDeleted()) {
					boolean newLine = lastDisplayed != 0 && lastDisplayed != leftLine.index();
					if (newLine) {
						System.out.println();
					}

					lastDisplayed = leftLine.index();
					String leftLineNumber = String.format(lineFormat, action.left().index());
					String output = String.format("%s\t- %s %s%s", RED_FONT, leftLineNumber, action.left().value(), NO_COLOR_FONT);
					System.out.println(output);
				}
			}
		}

		lastDisplayed = 0;
		List<Line> right = lines.right();
		for (int i = 0; i < right.size(); i++) {
			Line rightLine = right.get(i);
			Action action = actions.right().get(i);
			if (action.type() == Action.Type.ADDED) {
				if (options.consoleDisplayAdded()) {
					boolean newLine = lastDisplayed == 0 || lastDisplayed != rightLine.index();
					if (newLine) {
						System.out.println();
					}

					lastDisplayed = rightLine.index();
					String rightlineNumber = String.format(lineFormat, action.right().index());
					String output = String.format("%s\t+ %s %s%s", GREEN_FONT, rightlineNumber, action.right().value(), NO_COLOR_FONT);
					System.out.println(output);
				}
			}
		}
	}
}
