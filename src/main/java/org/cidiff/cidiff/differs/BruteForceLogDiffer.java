package org.cidiff.cidiff.differs;

import org.cidiff.cidiff.Action;
import org.cidiff.cidiff.Line;
import org.cidiff.cidiff.LogDiffer;
import org.cidiff.cidiff.Metric;
import org.cidiff.cidiff.Options;
import org.cidiff.cidiff.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public final class BruteForceLogDiffer implements LogDiffer {
	private static final Pattern EMPTY = Pattern.compile("\\s*");

	private final double rewriteMin;
	private final boolean skipEmpty;

	public BruteForceLogDiffer() {
		rewriteMin = Options.getRewriteMin();
		skipEmpty = Options.getSkipEmptyLines();
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
		for (int i = 0; i < leftLines.size(); i++) {
			Line leftLine = leftLines.get(i);
			for (int j = 0; j < rightLines.size(); j++) {
				Line rightLine = rightLines.get(j);
				if (!rightActions.get(j).isEmpty()) {
					continue;
				}

				if (skipEmpty && EMPTY.matcher(leftLine.value()).matches()) {
					leftActions.set(i, Action.skipped(leftLine));
					break;
				}
				if (leftLine.hasSameValue(rightLine)) {
					Action action = Action.unchanged(leftLine, rightLine, 1);
					leftActions.set(i, action);
					rightActions.set(j, action);
					break;
				}
			}
		}

		// Identify updated lines
		for (int i = 0; i < leftLines.size(); i++) {
			Line leftLine = leftLines.get(i);
			if (!leftActions.get(i).isEmpty()) {
				continue;
			}

			for (int j = 0; j < rightLines.size(); j++) {
				Line rightLine = rightLines.get(j);
				if (!rightActions.get(j).isEmpty()) {
					continue;
				}

				double sim = Options.metric().sim(leftLine, rightLine);
				if (sim >= rewriteMin) {
					Action action = Action.updated(leftLine, rightLine, sim);
					leftActions.set(i, action);
					rightActions.set(j, action);
					break;
				}
			}
		}

		// Identify deleted lines
		IntStream.range(0, leftLines.size())
				.filter(i -> leftActions.get(i).isEmpty())
				.forEach(i -> leftActions.set(i, Action.deleted(leftLines.get(i))));

		// Identify added lines
		IntStream.range(0, rightLines.size())
				.filter(i -> rightActions.get(i).isEmpty())
				.forEach(i -> {
					if (skipEmpty && EMPTY.matcher(rightLines.get(i).value()).matches()) {
						rightActions.set(i, Action.skipped(rightLines.get(i)));
					} else {
						rightActions.set(i, Action.added(rightLines.get(i)));
					}
				});

		return new Pair<>(leftActions, rightActions);
	}
}
