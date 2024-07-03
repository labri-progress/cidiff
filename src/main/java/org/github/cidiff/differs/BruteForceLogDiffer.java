package org.github.cidiff.differs;

import org.github.cidiff.Action;
import org.github.cidiff.Line;
import org.github.cidiff.LogDiffer;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public final class BruteForceLogDiffer implements LogDiffer {

	@Override
	public Pair<List<Action>> diff(List<Line> leftLines, List<Line> rightLines, Options options) {
		List<Action> leftActions = new ArrayList<>();
		for (int i = 0; i < leftLines.size(); i++) {
			leftActions.add(Action.NONE);
		}
		List<Action> rightActions = new ArrayList<>();
		for (int i = 0; i < rightLines.size(); i++) {
			rightActions.add(Action.NONE);
		}

		// Identify unchanged lines
		for (int i = 0; i < leftLines.size(); i++) {
			Line leftLine = leftLines.get(i);
			for (int j = 0; j < rightLines.size(); j++) {
				Line rightLine = rightLines.get(j);
				if (!rightActions.get(j).isNone()) {
					continue;
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
			if (!leftActions.get(i).isNone()) {
				continue;
			}

			for (int j = 0; j < rightLines.size(); j++) {
				Line rightLine = rightLines.get(j);
				if (!rightActions.get(j).isNone()) {
					continue;
				}

				double sim = options.metric().sim(leftLine.value(), rightLine.value());
				if (sim >= options.rewriteMin()) {
					Action action = Action.updated(leftLine, rightLine, sim);
					leftActions.set(i, action);
					rightActions.set(j, action);
					break;
				}
			}
		}

		// Identify deleted lines
		IntStream.range(0, leftLines.size())
				.filter(i -> leftActions.get(i).isNone())
				.forEach(i -> leftActions.set(i, Action.deleted(leftLines.get(i))));

		// Identify added lines
		IntStream.range(0, rightLines.size())
				.filter(i -> rightActions.get(i).isNone())
				.forEach(i -> rightActions.set(i, Action.added(rightLines.get(i))));

		return new Pair<>(leftActions, rightActions);
	}
}
