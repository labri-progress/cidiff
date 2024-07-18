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
				List.of("UNCHANGED:0", "UNCHANGED:1", "DELETED"),
				List.of("UNCHANGED:0", "UNCHANGED:1")
		));
	}

	@Test
	void added() {
		List<Line> left = TestHelpers.makeLog("Foo", "Bar");
		List<Line> right = TestHelpers.makeLog("Foo", "Bar", "Foo");
		final var d = new SeedDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options());

		assertActions(actions, Pair.of(
				List.of("UNCHANGED:0", "UNCHANGED:1"),
				List.of("UNCHANGED:0", "UNCHANGED:1", "ADDED")
		));
	}

	@Test
	void unchanged() {
		List<Line> left = TestHelpers.makeLog("Foo", "Bar", "Baz");
		List<Line> right = TestHelpers.makeLog("Foo", "Bar", "Baz");
		final var d = new SeedDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options());

		assertActions(actions, Pair.of(
				List.of("UNCHANGED:0", "UNCHANGED:1", "UNCHANGED:2"),
				List.of("UNCHANGED:0", "UNCHANGED:1", "UNCHANGED:2")
		));
	}

	@Test
	void unchanged2() {
		List<Line> left = TestHelpers.makeLog("Foo", "Bar", "Foo", "Bar");
		List<Line> right = TestHelpers.makeLog("Foo", "Bar");
		final var d = new SeedDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options());

		assertActions(actions, Pair.of(
				List.of("DELETED", "DELETED", "UNCHANGED:0", "UNCHANGED:1"),
				List.of("UNCHANGED:2", "UNCHANGED:3")
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
				List.of("UNCHANGED:0", "UNCHANGED:1", "UPDATED:2", "DELETED"),
				List.of("UNCHANGED:0", "UNCHANGED:1", "UPDATED:2", "ADDED")
		));
	}

	@Test
	void moved() {
		List<Line> left = TestHelpers.makeLog("the fish is in the pond", "moved with a change", "Foo", "Bar", "hello world", "another line");
		List<Line> right = TestHelpers.makeLog("Foo", "Bar", "hello everyone", "the fish is in the pond", "moved with no change", "another line");
		final var d = new SeedDiffer();
		final Pair<List<Action>> actions = d.diff(left, right, new Options());

		assertActions(actions, Pair.of(
				List.of("MOVED_UNCHANGED:3", "MOVED_UPDATED:4", "UNCHANGED:0", "UNCHANGED:1", "UPDATED:2", "UNCHANGED:5"),
				List.of("UNCHANGED:2", "UNCHANGED:3", "UPDATED:4", "MOVED_UNCHANGED:0", "MOVED_UPDATED:1", "UNCHANGED:5")
		));
	}

}
