package org.github.cidiff.differs;

import org.github.cidiff.Action;
import org.github.cidiff.LCS;
import org.github.cidiff.Line;
import org.github.cidiff.LogDiffer;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public final class LcsDiffer implements LogDiffer {

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

		// Identify unchanged/updated lines
		final List<Pair<Line>> lcs = LCS.myers(leftLines, rightLines, (a, b) -> options.metric().sim(a.value(), b.value()) >= options.rewriteMin());
		for (Pair<Line> pair : lcs) {
			Action action;
			if (pair.left().hasSameValue(pair.right())) {
				action = Action.unchanged(pair.left(), pair.right(), 1);
			} else {
				action = Action.updated(pair.left(), pair.right(), options.metric().sim(pair.left().value(), pair.right().value()));
			}
			leftActions.set(pair.left().index() - 1, action);
			rightActions.set(pair.right().index() - 1, action);
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
