package org.github.gumtreediff.cidiff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.github.gumtreediff.cidiff.differs.BruteForceLogDiffer;
import org.junit.jupiter.api.Test;

public class BruteForceLogDifferTest {
    @Test
    void testUnchangedCode1() {
        final var leftLog = TestHelpers.makeLog("Foo", "Bar", "Foo");
        final var rightLog = TestHelpers.makeLog("Foo", "Bar");
        final var d = new BruteForceLogDiffer(TestHelpers.makeOptions());
        final var actions = d.diff(new Pair<>(leftLog, rightLog));
        assertEquals(Action.unchanged(leftLog.get(0), rightLog.get(0)), actions.left.get(leftLog.get(0)));
        assertEquals(Action.unchanged(leftLog.get(0), rightLog.get(0)), actions.right.get(rightLog.get(0)));
        assertEquals(Action.unchanged(leftLog.get(1), rightLog.get(1)), actions.left.get(leftLog.get(1)));
        assertEquals(Action.unchanged(leftLog.get(1), rightLog.get(1)), actions.right.get(rightLog.get(1)));
        assertEquals(Action.deleted(leftLog.get(2)), actions.left.get(leftLog.get(2)));
    }

    @Test
    void testUnchangedCode2() {
        final var leftLog = TestHelpers.makeLog("Foo", "Foo");
        final var rightLog = TestHelpers.makeLog("Foo", "Foo", "Foo");
        final var d = new BruteForceLogDiffer(TestHelpers.makeOptions());
        final var actions = d.diff(new Pair<>(leftLog, rightLog));
        assertEquals(Action.unchanged(leftLog.get(0), rightLog.get(0)), actions.left.get(leftLog.get(0)));
        assertEquals(Action.unchanged(leftLog.get(0), rightLog.get(0)), actions.right.get(rightLog.get(0)));
        assertEquals(Action.unchanged(leftLog.get(1), rightLog.get(1)), actions.left.get(leftLog.get(1)));
        assertEquals(Action.unchanged(leftLog.get(1), rightLog.get(1)), actions.right.get(rightLog.get(1)));
        assertEquals(Action.added(rightLog.get(2)), actions.right.get(rightLog.get(2)));
    }

    @Test
    void testDeletedCode() {
        final var leftLog = TestHelpers.makeLog("Foo", "Baz", "Foo");
        final var rightLog = TestHelpers.makeLog("Foo", "Foo");
        final var d = new BruteForceLogDiffer(TestHelpers.makeOptions());
        final var actions = d.diff(new Pair<>(leftLog, rightLog));
        assertEquals(Action.unchanged(leftLog.get(0), rightLog.get(0)), actions.left.get(leftLog.get(0)));
        assertEquals(Action.unchanged(leftLog.get(0), rightLog.get(0)), actions.right.get(rightLog.get(0)));
        assertEquals(Action.deleted(leftLog.get(1)), actions.left.get(leftLog.get(1)));
        assertEquals(Action.unchanged(leftLog.get(2), rightLog.get(1)), actions.left.get(leftLog.get(2)));
        assertEquals(Action.unchanged(leftLog.get(2), rightLog.get(1)), actions.right.get(rightLog.get(1)));
    }

    @Test
    void testAddedCode() {
        final var leftLog = TestHelpers.makeLog("Foo", "Foo");
        final var rightLog = TestHelpers.makeLog("Foo", "Baz", "Foo");
        final var d = new BruteForceLogDiffer(TestHelpers.makeOptions());
        final var actions = d.diff(new Pair<>(leftLog, rightLog));
        assertEquals(Action.unchanged(leftLog.get(0), rightLog.get(0)), actions.left.get(leftLog.get(0)));
        assertEquals(Action.unchanged(leftLog.get(0), rightLog.get(0)), actions.right.get(rightLog.get(0)));
        assertEquals(Action.added(rightLog.get(1)), actions.right.get(rightLog.get(1)));
        assertEquals(Action.unchanged(leftLog.get(1), rightLog.get(2)), actions.left.get(leftLog.get(1)));
        assertEquals(Action.unchanged(leftLog.get(1), rightLog.get(2)), actions.right.get(rightLog.get(2)));
    }

    @Test
    void testUpdatedCode() {
        final var leftLog = TestHelpers.makeLog("Build status failed",
                "Running time: 22s", "Foo Bar", "Foo", "Foo Foo");
        final var rightLog = TestHelpers.makeLog("Build status OK",
                "Running times: 22s", "Foo Baz", "Bar", "Fooo Fooo");
        final var d = new BruteForceLogDiffer(TestHelpers.makeOptions());
        final var actions = d.diff(new Pair<>(leftLog, rightLog));

        assertEquals(Action.updated(leftLog.get(0), rightLog.get(0)), actions.left.get(leftLog.get(0)));
        assertEquals(Action.updated(leftLog.get(0), rightLog.get(0)), actions.right.get(rightLog.get(0)));
        assertEquals(Action.updated(leftLog.get(1), rightLog.get(1)), actions.left.get(leftLog.get(1)));
        assertEquals(Action.updated(leftLog.get(1), rightLog.get(1)), actions.right.get(rightLog.get(1)));
        assertEquals(Action.updated(leftLog.get(2), rightLog.get(2)), actions.left.get(leftLog.get(2)));
        assertEquals(Action.updated(leftLog.get(2), rightLog.get(2)), actions.right.get(rightLog.get(2)));
        assertEquals(Action.deleted(leftLog.get(3)), actions.left.get(leftLog.get(3)));
        assertEquals(Action.added(rightLog.get(3)), actions.right.get(rightLog.get(3)));
        assertEquals(Action.deleted(leftLog.get(4)), actions.left.get(leftLog.get(4)));
        assertEquals(Action.added(rightLog.get(4)), actions.right.get(rightLog.get(4)));
    }
}
