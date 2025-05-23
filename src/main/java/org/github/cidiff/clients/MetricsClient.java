package org.github.cidiff.clients;


import org.github.cidiff.Action;
import org.github.cidiff.Line;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;

import java.util.List;

public class MetricsClient extends AbstractDiffClient {

	public MetricsClient(Pair<List<Line>> lines, Pair<List<Action>> actions) {
		super(lines, actions);
	}

	@Override
	public void execute(Options options) {
		System.out.println("Left log: " + lines.left().size() + " lines.");
		System.out.println("Right log: " + lines.right().size() + " lines.");
		final Metrics metrics = getMetrics();
		System.out.println("Added lines: " + metrics.added + " lines.");
		System.out.println("Deleted lines: " + metrics.deleted + " lines.");
		System.out.println("Unchanged lines: " + metrics.unchanged + " lines.");
		System.out.println("Updated lines: " + metrics.updated + " lines.");
		System.out.println("Moved lines: " + metrics.moved + " lines.");
		System.out.println("Total actions: " + (metrics.added + metrics.updated + metrics.deleted + metrics.moved));
	}

	private Metrics getMetrics() {
		final Metrics metrics = new Metrics();
		for (Action a : actions.left())
			if (a.type() == Action.Type.DELETED)
				metrics.deleted++;
			else if (a.type() == Action.Type.UNCHANGED)
				metrics.unchanged++;
			else if (a.type() == Action.Type.UPDATED)
				metrics.updated++;
			else if (a.type() == Action.Type.MOVED_UPDATED || a.type() == Action.Type.MOVED_UNCHANGED)
				metrics.moved++;

		for (Action a : actions.right())
			if (a.type() == Action.Type.ADDED)
				metrics.added++;

		return metrics;
	}

	private static class Metrics {
		int added = 0;
		int updated = 0;
		int unchanged = 0;
		int deleted = 0;
		int moved = 0;
	}

}
