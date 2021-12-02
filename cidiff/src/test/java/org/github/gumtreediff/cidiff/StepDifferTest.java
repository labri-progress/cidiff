package org.github.gumtreediff.cidiff;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class StepDifferTest {
    @Test
    void rewriteSimTest() {
        assertEquals(0.0, StepDiffer.rewriteSim("A B C", "A B C D"), 0.001);
        assertEquals(0.666, StepDiffer.rewriteSim("A B C", "A EE C"), 0.001);
        assertEquals(0.333, StepDiffer.rewriteSim("A B C", "A EE FF"), 0.001);
        assertEquals(0.0, StepDiffer.rewriteSim("A B C", "GG EE FF"), 0.001);
    }

    @Test
    void testUnchangedCode1() {
        List<String> leftLines = Arrays.asList("Foo", "Bar", "Foo");
        List<String> rightLines = Arrays.asList("Foo", "Bar");
        StepDiffer d = new StepDiffer(leftLines, rightLines);
        assertEquals(Action.unchanged(0, 0), d.leftActions[0]);
        assertEquals(Action.unchanged(0, 0), d.rightActions[0]);
        assertEquals(Action.unchanged(1, 1), d.leftActions[1]);
        assertEquals(Action.unchanged(1, 1), d.rightActions[1]);
        assertEquals(Action.deleted(2), d.leftActions[2]);
    }

    @Test
    void testUnchangedCode2() {
        List<String> leftLines = Arrays.asList("Foo", "Foo");
        List<String> rightLines = Arrays.asList("Foo", "Foo", "Foo");
        StepDiffer d = new StepDiffer(leftLines, rightLines);
        assertEquals(Action.unchanged(0, 0), d.leftActions[0]);
        assertEquals(Action.unchanged(0, 0), d.rightActions[0]);
        assertEquals(Action.unchanged(1, 1), d.leftActions[1]);
        assertEquals(Action.unchanged(1, 1), d.rightActions[1]);
        assertEquals(Action.added(2), d.rightActions[2]);
    }

    @Test
    void testDeletedCode() {
        List<String> leftLines = Arrays.asList("Foo", "Baz", "Foo");
        List<String> rightLines = Arrays.asList("Foo", "Foo");
        StepDiffer d = new StepDiffer(leftLines, rightLines);
        assertEquals(Action.unchanged(0, 0), d.leftActions[0]);
        assertEquals(Action.unchanged(0, 0), d.rightActions[0]);
        assertEquals(Action.deleted(1), d.leftActions[1]);
        assertEquals(Action.unchanged(2, 1), d.leftActions[2]);
        assertEquals(Action.unchanged(2, 1), d.rightActions[1]);
    }

    @Test
    void testAddedCode() {
        List<String> leftLines = Arrays.asList("Foo", "Foo");
        List<String> rightLines = Arrays.asList("Foo", "Baz", "Foo");
        StepDiffer d = new StepDiffer(leftLines, rightLines);
        assertEquals(Action.unchanged(0, 0), d.leftActions[0]);
        assertEquals(Action.unchanged(0, 0), d.rightActions[0]);
        assertEquals(Action.added(1), d.rightActions[1]);
        assertEquals(Action.unchanged(1, 2), d.leftActions[1]);
        assertEquals(Action.unchanged(1, 2), d.rightActions[2]);
    }

    @Test
    void testUpdatedCode() {
        List<String> leftLines = Arrays.asList("Build status failed", "Running time: 22s", "Foo Bar", "Foo", "Foo Foo");
        List<String> rightLines = Arrays.asList("Build status OK", "Running times: 22s", "Foo Baz", "Bar", "Fooo Fooo");
        StepDiffer d = new StepDiffer(leftLines, rightLines);
        assertEquals(Action.updated(0, 0), d.leftActions[0]);
        assertEquals(Action.updated(0, 0), d.rightActions[0]);
        assertEquals(Action.updated(1, 1), d.leftActions[1]);
        assertEquals(Action.updated(1, 1), d.rightActions[1]);
        assertEquals(Action.updated(2, 2), d.leftActions[2]);
        assertEquals(Action.updated(2, 2), d.rightActions[2]);
        assertEquals(Action.updated(3, 3), d.leftActions[3]);
        assertEquals(Action.updated(3, 3), d.rightActions[3]);
        assertEquals(Action.deleted(4), d.leftActions[4]);
        assertEquals(Action.added(4), d.rightActions[4]);
    }
}
