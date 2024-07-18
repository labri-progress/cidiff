package org.github.cidiff;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestHelpers {

	private TestHelpers() {
	}

	public static List<Line> makeLog(String... lines) {
		final List<Line> log = new ArrayList<>(lines.length);
		for (int i = 0; i < lines.length; i++)
			log.add(new Line(i, lines[i]));
		return log;
	}

	/**
	 * @param actual the actions to test
	 * @param expected string are either "ADDED", "DELETED", or "<action>:<left_index>"
	 */
	public static void assertActions(Pair<List<Action>> actual, Pair<List<String>> expected) {
		assertActions(true, actual.left(), expected.left());
		assertActions(false, actual.right(), expected.right());
	}

	private static void assertActions(boolean isLeft, List<Action> actual, List<String> expected) {
		Function<Action, Integer> getIndex = action -> isLeft ? action.right().index() : action.left().index();
		String logName = isLeft ? "left" : "right";
		for (int i = 0; i < expected.size(); i++) {
			String str = expected.get(i);
			if (str.equals("ADDED") || str.equals("DELETED")) {
				String message = String.format("Expected at %s line %d \"%s\", obtained \"%s\"", logName, i + 1, str, format(isLeft, actual.get(i)));
				assertEquals(str, actual.get(i).type().name(), message);
			} else {
				String[] tokens = str.split(":");
				String message = String.format("Expected at %s line %d \"%s\", obtained \"%s\"", logName, i + 1, str, format(isLeft, actual.get(i)));
				assertEquals(tokens[0], actual.get(i).type().name(), message);
				assertEquals(Integer.parseInt(tokens[1]), getIndex.apply(actual.get(i)), message);
			}
		}
	}

	private static String format(boolean isLeft, Action action) {
		return switch (action.type()) {
			case ADDED, DELETED -> action.type().name();
			default -> action.type().name() + ":" + (isLeft ? action.right().index() : action.left().index());
		};
	}

}
