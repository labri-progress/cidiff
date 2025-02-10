package org.github.cidiff.differs;

import org.github.cidiff.Action;
import org.github.cidiff.LCS;
import org.github.cidiff.Line;
import org.github.cidiff.LogDiffer;
import org.github.cidiff.Metric;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class VariableLcsDiffer implements LogDiffer {

	@Override
	public Pair<List<Action>> diff(List<Line> leftLines, List<Line> rightLines, Options options) {
		Action[] leftActions = new Action[leftLines.size()];
		Arrays.fill(leftActions, Action.NONE);
		Action[] rightActions = new Action[rightLines.size()];
		Arrays.fill(rightActions, Action.NONE);

		// Identify unchanged/updated lines
		Metric metric = options.metric();
		double minSimilarity = options.rewriteMin();
		final List<Pair<Line>> lcs = LCS.myers(leftLines, rightLines, (a, b) -> metric.sim(a.value(), b.value()) >= minSimilarity);
		for (Pair<Line> pair : lcs) {
			Action action;
			if (pair.left().hasSameValue(pair.right())) {
				action = Action.unchanged(pair.left(), pair.right(), 1);
			} else {
				action = Action.updated(pair.left(), pair.right(), metric.sim(pair.left().value(), pair.right().value()));
			}
			leftActions[pair.left().index()] = action;
			rightActions[pair.right().index()] = action;
		}


		// Identify deleted lines
		IntStream.range(0, leftLines.size())
				.filter(j -> leftActions[j].isNone())
				.forEach(j -> leftActions[j] = Action.deleted(leftLines.get(j)));

		// Identify added lines
		IntStream.range(0, rightLines.size())
				.filter(j -> rightActions[j].isNone())
				.forEach(j -> rightActions[j] = Action.added(rightLines.get(j)));

		return new Pair<>(new ArrayList<>(Arrays.asList(leftActions)), new ArrayList<>(Arrays.asList(rightActions)));
	}
}
