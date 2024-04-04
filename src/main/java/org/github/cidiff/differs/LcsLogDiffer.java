package org.github.cidiff.differs;

import org.github.cidiff.Action;
import org.github.cidiff.Line;
import org.github.cidiff.LogDiffer;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;
import org.github.cidiff.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public final class LcsLogDiffer implements LogDiffer {

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
		final List<Line> lcs = Utils.lcs(leftLines, rightLines, (a, b) -> options.metric().sim(a.value(), b.value()) >= options.rewriteMin());
		int i = 0;
		for (Line leftLine : lcs) {
			// TODO: 3/29/24 @nhubner fix this, it doesn't actually produce the real lcs, it may produce a different one if two lines in the left log is similar to the same line in the right log
			for (int j = i; j < rightLines.size(); j++) {
				if (options.metric().sim(leftLine.value(), rightLines.get(i).value()) >= options.rewriteMin()) {
					Action action;
					if (leftLine.hasSameValue(rightLines.get(i))) {
						action = Action.unchanged(leftLine, rightLines.get(i), 1);
					} else {
						action = Action.updated(leftLine, rightLines.get(i), options.metric().sim(leftLine.value(), rightLines.get(i).value()));
					}
					leftActions.set(leftLine.index()-1, action);
					rightActions.set(i, action);
				}
				i++;
			}
		}


		// Identify deleted lines
		IntStream.range(0, leftLines.size())
				.filter(j -> leftActions.get(j).isNone())
				.forEach(j -> leftActions.set(j, Action.deleted(leftLines.get(j))));

		// Identify added lines
		IntStream.range(0, rightLines.size())
				.filter(j -> rightActions.get(j).isNone())
				.forEach(j -> rightActions.set(j, Action.added(rightLines.get(j))));

		return new Pair<>(leftActions, rightActions);
	}
}
