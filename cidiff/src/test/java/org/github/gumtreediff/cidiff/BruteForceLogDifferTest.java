package org.github.gumtreediff.cidiff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.github.gumtreediff.cidiff.differs.BruteForceLogDiffer;
import org.junit.jupiter.api.Test;

public class BruteForceLogDifferTest {
    private static final Properties DEFAULT_OPTIONS = new Properties();

    @Test
    void testUnchangedCode1() {
        final List<String> leftLines = Arrays.asList("Foo", "Bar", "Foo");
        final List<String> rightLines = Arrays.asList("Foo", "Bar");
        final LogDiffer d = new BruteForceLogDiffer(DEFAULT_OPTIONS);
        final Pair<Action[]> actions = d.diff(new Pair<>(leftLines, rightLines));
        assertEquals(Action.unchanged(0, 0), actions.left[0]);
        assertEquals(Action.unchanged(0, 0), actions.right[0]);
        assertEquals(Action.unchanged(1, 1), actions.left[1]);
        assertEquals(Action.unchanged(1, 1), actions.right[1]);
        assertEquals(Action.deleted(2), actions.left[2]);
    }

    @Test
    void testUnchangedCode2() {
        final List<String> leftLines = Arrays.asList("Foo", "Foo");
        final List<String> rightLines = Arrays.asList("Foo", "Foo", "Foo");
        final LogDiffer d = new BruteForceLogDiffer(DEFAULT_OPTIONS);
        final Pair<Action[]> actions = d.diff(new Pair<>(leftLines, rightLines));
        assertEquals(Action.unchanged(0, 0), actions.left[0]);
        assertEquals(Action.unchanged(0, 0), actions.right[0]);
        assertEquals(Action.unchanged(1, 1), actions.left[1]);
        assertEquals(Action.unchanged(1, 1), actions.right[1]);
        assertEquals(Action.added(2), actions.right[2]);
    }

    @Test
    void testDeletedCode() {
        final List<String> leftLines = Arrays.asList("Foo", "Baz", "Foo");
        final List<String> rightLines = Arrays.asList("Foo", "Foo");
        final LogDiffer d = new BruteForceLogDiffer(DEFAULT_OPTIONS);
        final Pair<Action[]> actions = d.diff(new Pair<>(leftLines, rightLines));
        assertEquals(Action.unchanged(0, 0), actions.left[0]);
        assertEquals(Action.unchanged(0, 0), actions.right[0]);
        assertEquals(Action.deleted(1), actions.left[1]);
        assertEquals(Action.unchanged(2, 1), actions.left[2]);
        assertEquals(Action.unchanged(2, 1), actions.right[1]);
    }

    @Test
    void testAddedCode() {
        final List<String> leftLines = Arrays.asList("Foo", "Foo");
        final List<String> rightLines = Arrays.asList("Foo", "Baz", "Foo");
        final LogDiffer d = new BruteForceLogDiffer(DEFAULT_OPTIONS);
        final Pair<Action[]> actions = d.diff(new Pair<>(leftLines, rightLines));
        assertEquals(Action.unchanged(0, 0), actions.left[0]);
        assertEquals(Action.unchanged(0, 0), actions.right[0]);
        assertEquals(Action.added(1), actions.right[1]);
        assertEquals(Action.unchanged(1, 2), actions.left[1]);
        assertEquals(Action.unchanged(1, 2), actions.right[2]);
    }

    @Test
    void testUpdatedCode() {
        final List<String> leftLines = Arrays.asList("Build status failed",
                "Running time: 22s", "Foo Bar", "Foo", "Foo Foo");
        final List<String> rightLines = Arrays.asList("Build status OK",
                "Running times: 22s", "Foo Baz", "Bar", "Fooo Fooo");
        final LogDiffer d = new BruteForceLogDiffer(DEFAULT_OPTIONS);
        final Pair<Action[]> actions = d.diff(new Pair<>(leftLines, rightLines));
        assertEquals(Action.updated(0, 0), actions.left[0]);
        assertEquals(Action.updated(0, 0), actions.right[0]);
        assertEquals(Action.updated(1, 1), actions.left[1]);
        assertEquals(Action.updated(1, 1), actions.right[1]);
        assertEquals(Action.updated(2, 2), actions.left[2]);
        assertEquals(Action.updated(2, 2), actions.right[2]);
        assertEquals(Action.deleted(3), actions.left[3]);
        assertEquals(Action.added(3), actions.right[3]);
        assertEquals(Action.deleted(4), actions.left[4]);
        assertEquals(Action.added(4), actions.right[4]);
    }
}
