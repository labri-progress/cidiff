package org.github.cidiff.differs;

import org.github.cidiff.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.github.cidiff.TestHelpers.assertActions;

class SeedDifferTest {

	@Test
	void deleted() {
		List<Line> left = TestHelpers.makeLog("Foo", "Bar", "Foo");
		List<Line> right = TestHelpers.makeLog("Foo", "Bar");
		final var d = new SeedDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options());

		assertActions(actions, Pair.of(
				List.of("UNCHANGED:1", "UNCHANGED:2", "DELETED"),
				List.of("UNCHANGED:1", "UNCHANGED:2")
		));
	}

	@Test
	void added() {
		List<Line> left = TestHelpers.makeLog("Foo", "Bar");
		List<Line> right = TestHelpers.makeLog("Foo", "Bar", "Foo");
		final var d = new SeedDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options());

		assertActions(actions, Pair.of(
				List.of("UNCHANGED:1", "UNCHANGED:2"),
				List.of("UNCHANGED:1", "UNCHANGED:2", "ADDED")
		));
	}

	@Test
	void unchanged() {
		List<Line> left = TestHelpers.makeLog("Foo", "Bar", "Baz");
		List<Line> right = TestHelpers.makeLog("Foo", "Bar", "Baz");
		final var d = new SeedDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options());

		assertActions(actions, Pair.of(
				List.of("UNCHANGED:1", "UNCHANGED:2", "UNCHANGED:3"),
				List.of("UNCHANGED:1", "UNCHANGED:2", "UNCHANGED:3")
		));
	}

	@Test
	void unchanged2() {
		List<Line> left = TestHelpers.makeLog("Foo", "Bar", "Foo", "Bar");
		List<Line> right = TestHelpers.makeLog("Foo", "Bar");
		final var d = new SeedDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options());

		assertActions(actions, Pair.of(
				List.of("DELETED", "DELETED", "DELETED", "DELETED"),
				List.of("ADDED", "ADDED")
		));
	}

	@Test
	void empty() {
		List<Line> left = TestHelpers.makeLog("Foo", "Bar");
		List<Line> right = TestHelpers.makeLog();
		final var d = new SeedDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options());

		assertActions(actions, Pair.of(
				List.of("DELETED", "DELETED"),
				List.of()
		));
	}

	@Test
	void updated() {
		List<Line> left = TestHelpers.makeLog("Foo", "Bar", "hello world", "the fish is in the pond");
		List<Line> right = TestHelpers.makeLog("Foo", "Bar", "hello everyone", "the fish is not in the pond");
		final var d = new SeedDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options());

		assertActions(actions, Pair.of(
				List.of("UNCHANGED:1", "UNCHANGED:2", "UPDATED:3", "DELETED"),
				List.of("UNCHANGED:1", "UNCHANGED:2", "UPDATED:3", "ADDED")
		));
	}

	@Test
	void moved() {
		List<Line> left = TestHelpers.makeLog("the fish is in the pond", "moved with a change", "Foo", "Bar", "hello world", "another line");
		List<Line> right = TestHelpers.makeLog("Foo", "Bar", "hello everyone", "the fish is in the pond", "moved with no change", "another line");
		final var d = new SeedDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options());

		assertActions(actions, Pair.of(
				List.of("MOVED_UNCHANGED:4", "MOVED_UPDATED:5", "UNCHANGED:1", "UNCHANGED:2", "UPDATED:3", "UNCHANGED:6"),
				List.of("UNCHANGED:3", "UNCHANGED:4", "UPDATED:5", "MOVED_UNCHANGED:1", "MOVED_UPDATED:2", "UNCHANGED:6")
		));
	}

}