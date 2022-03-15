package org.github.gumtreediff.cidiff;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BruteForceLogDifferTest {
    private static final Properties options = new Properties();

    @Test
    void testUnchangedCode1() {
        List<String> leftLines = Arrays.asList("Foo", "Bar", "Foo");
        List<String> rightLines = Arrays.asList("Foo", "Bar");
        LogDiffer d = new BruteForceLogDiffer(options);
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
        LogDiffer d = new BruteForceLogDiffer(options);
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
        LogDiffer d = new BruteForceLogDiffer(options);
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
        LogDiffer d = new BruteForceLogDiffer(options);
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
        LogDiffer d = new BruteForceLogDiffer(options);
        Pair<Action[]> actions = d.diff(new Pair<>(leftLines, rightLines));
        assertEquals(Action.updated(0, 0), actions.left[0]);
        assertEquals(Action.updated(0, 0), actions.right[0]);
        assertEquals(Action.updated(1, 1), actions.left[1]);
        assertEquals(Action.updated(1, 1), actions.right[1]);
        assertEquals(Action.updated(2, 2), actions.left[2]);
        assertEquals(Action.updated(2, 2), actions.right[2]);
        assertEquals(Action.updated(3, 3), actions.left[3]);
        assertEquals(Action.updated(3, 3), actions.right[3]);
        assertEquals(Action.deleted(4), actions.left[4]);
        assertEquals(Action.added(4), actions.right[4]);
    }

    @Test
    void testUpdatedCode2() {
        List<String> leftLines = Arrays.asList("Distinct1", "Same1", "Same1", "Distinct1", "Same2", "Same2");
        List<String> rightLines = Arrays.asList("Distinct2", "Same1", "Same1", "Distinct2", "Same2", "Same2");
        LogDiffer d = new BruteForceLogDiffer(options);
        Pair<Action[]> actions = d.diff(new Pair<>(leftLines, rightLines));
        assertEquals(Action.updated(0, 0), actions.left[0]);
        assertEquals(Action.updated(0, 0), actions.right[0]);
        assertEquals(Action.unchanged(1, 1), actions.left[1]);
        assertEquals(Action.unchanged(1, 1), actions.right[1]);
        assertEquals(Action.unchanged(2, 2), actions.left[2]);
        assertEquals(Action.unchanged(2, 2), actions.right[2]);
        assertEquals(Action.updated(3, 3), actions.left[3]);
        assertEquals(Action.updated(3, 3), actions.right[3]);
        assertEquals(Action.unchanged(4, 4), actions.left[4]);
        assertEquals(Action.unchanged(4, 4), actions.right[4]);
    }
}
