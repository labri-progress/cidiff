package org.github.cidiff.differs;

import org.github.cidiff.Action;
import org.github.cidiff.Line;
import org.github.cidiff.Metric;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;
import org.github.cidiff.TestHelpers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.github.cidiff.TestHelpers.assertActions;
import static org.junit.jupiter.api.Assertions.*;

class LcsDifferTest {

	@Test
	void deleted() {
		List<Line> left = TestHelpers.makeLog("Foo", "Bar", "Foo");
		List<Line> right = TestHelpers.makeLog("Foo", "Bar");
		final var d = new LcsDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options().with(Options.METRIC, Metric.EQUALITY));

		assertActions(actions, Pair.of(
				List.of("UNCHANGED:0", "UNCHANGED:1", "DELETED"),
				List.of("UNCHANGED:0", "UNCHANGED:1")
		));
	}

	@Test
	void added() {
		List<Line> left = TestHelpers.makeLog("Foo", "Bar");
		List<Line> right = TestHelpers.makeLog("Foo", "Bar", "Foo");
		final var d = new LcsDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options().with(Options.METRIC, Metric.EQUALITY));

		assertActions(actions, Pair.of(
				List.of("UNCHANGED:0", "UNCHANGED:1"),
				List.of("UNCHANGED:0", "UNCHANGED:1", "ADDED")
		));
	}

	@Test
	void unchanged() {
		List<Line> left = TestHelpers.makeLog("Foo", "Bar", "Baz");
		List<Line> right = TestHelpers.makeLog("Foo", "Bar", "Baz");
		final var d = new LcsDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options().with(Options.METRIC, Metric.EQUALITY));

		assertActions(actions, Pair.of(
				List.of("UNCHANGED:0", "UNCHANGED:1", "UNCHANGED:2"),
				List.of("UNCHANGED:0", "UNCHANGED:1", "UNCHANGED:2")
		));
	}

	@Test
	void unchanged2() {
		List<Line> left = TestHelpers.makeLog("Foo", "Bar", "Foo", "Bar");
		List<Line> right = TestHelpers.makeLog("Foo", "Bar");
		final var d = new LcsDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options().with(Options.METRIC, Metric.EQUALITY));

		assertActions(actions, Pair.of(
				List.of("DELETED", "DELETED", "UNCHANGED:0", "UNCHANGED:1"),
				List.of("UNCHANGED:2", "UNCHANGED:3")
		));
	}

	@Test
	void empty() {
		List<Line> left = TestHelpers.makeLog("Foo", "Bar");
		List<Line> right = TestHelpers.makeLog();
		final var d = new LcsDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options().with(Options.METRIC, Metric.EQUALITY));

		assertActions(actions, Pair.of(
				List.of("DELETED", "DELETED"),
				List.of()
		));
	}

	@Test
	void multi() {
		List<Line> left = TestHelpers.makeLog("Foo", "Foo", "Bar", "Bar");
		List<Line> right = TestHelpers.makeLog("Foo", "Bar", "Baz");
		final var d = new LcsDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options().with(Options.METRIC, Metric.EQUALITY));
		// this will work only if the lcs used is myers' algorithm. hirschberg's algorithm doesn't produce the same lcs
		assertActions(actions, Pair.of(
				List.of("DELETED", "UNCHANGED:0", "UNCHANGED:1",  "DELETED"),
				List.of("UNCHANGED:1", "UNCHANGED:2", "ADDED")
		));
	}

}