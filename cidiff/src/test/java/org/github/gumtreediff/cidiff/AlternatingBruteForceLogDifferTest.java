package org.github.gumtreediff.cidiff;

import static org.junit.jupiter.api.Assertions.*;

import org.github.gumtreediff.cidiff.differs.AlternatingBruteForceLogDiffer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AlternatingBruteForceLogDifferTest {
    private static final Properties options = new Properties();

    @Test
    void testUnchangedCode1() {
        List<String> leftLines = Arrays.asList("Foo", "Bar", "Foo");
        List<String> rightLines = Arrays.asList("Foo", "Bar");
        AlternatingBruteForceLogDiffer d = new AlternatingBruteForceLogDiffer(options);
        Pair<Action[]> actions = d.diff(new Pair<>(leftLines, rightLines));
        assertEquals(Action.unchanged(0, 0), actions.left[0]);
        assertEquals(Action.unchanged(0, 0), actions.right[0]);
        assertEquals(Action.unchanged(1, 1), actions.left[1]);
        assertEquals(Action.unchanged(1, 1), actions.right[1]);
        assertEquals(Action.deleted(2), actions.left[2]);
    }

    @Test
    void testUnchangedCode2() {
        List<String> leftLines = Arrays.asList("Foo", "Foo");
        List<String> rightLines = Arrays.asList("Foo", "Foo", "Foo");
        LogDiffer d = new AlternatingBruteForceLogDiffer(options);
        Pair<Action[]> actions = d.diff(new Pair<>(leftLines, rightLines));
        assertEquals(Action.unchanged(0, 0), actions.left[0]);
        assertEquals(Action.unchanged(0, 0), actions.right[0]);
        assertEquals(Action.unchanged(1, 1), actions.left[1]);
        assertEquals(Action.unchanged(1, 1), actions.right[1]);
        assertEquals(Action.added(2), actions.right[2]);
    }

    @Test
    void testDeletedCode() {
        List<String> leftLines = Arrays.asList("Foo", "Baz", "Foo");
        List<String> rightLines = Arrays.asList("Foo", "Foo");
        LogDiffer d = new AlternatingBruteForceLogDiffer(options);
        Pair<Action[]> actions = d.diff(new Pair<>(leftLines, rightLines));
        assertEquals(Action.unchanged(0, 0), actions.left[0]);
        assertEquals(Action.unchanged(0, 0), actions.right[0]);
        assertEquals(Action.deleted(1), actions.left[1]);
        assertEquals(Action.unchanged(2, 1), actions.left[2]);
        assertEquals(Action.unchanged(2, 1), actions.right[1]);
    }

    @Test
    void testAddedCode() {
        List<String> leftLines = Arrays.asList("Foo", "Foo");
        List<String> rightLines = Arrays.asList("Foo", "Baz", "Foo");
        LogDiffer d = new AlternatingBruteForceLogDiffer(options);
        Pair<Action[]> actions = d.diff(new Pair<>(leftLines, rightLines));
        assertEquals(Action.unchanged(0, 0), actions.left[0]);
        assertEquals(Action.unchanged(0, 0), actions.right[0]);
        assertEquals(Action.added(1), actions.right[1]);
        assertEquals(Action.unchanged(1, 2), actions.left[1]);
        assertEquals(Action.unchanged(1, 2), actions.right[2]);
    }

    @Test
    void testUpdatedCode() {
        List<String> leftLines = Arrays.asList("Build status failed", "Running time: 22s", "Foo Bar", "Foo", "Foo Foo");
        List<String> rightLines = Arrays.asList("Build status OK", "Running times: 22s", "Foo Baz", "Bar", "Fooo Fooo");
        LogDiffer d = new AlternatingBruteForceLogDiffer(options);
        Pair<Action[]> actions = d.diff(new Pair<>(leftLines, rightLines));
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
