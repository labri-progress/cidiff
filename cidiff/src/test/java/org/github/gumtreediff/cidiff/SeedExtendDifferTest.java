package org.github.gumtreediff.cidiff;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeedExtendDifferTest {
    private static final Properties getOptions() {
        Properties options = new Properties();
        options.setProperty(Options.DIFFER_SEED_BLOCK, "2");
        options.setProperty(Options.DIFFER_SEED_WINDOW, "5");
        return options;
    }

    @Test
    void testUnchangedCode1() {
        List<String> leftLines = Arrays.asList("Foo", "Bar");
        List<String> rightLines = Arrays.asList("Foo", "Bar");
        StepDiffer d = new SeedExtendDiffer(getOptions());
        Pair<Action[]> actions = d.diffStep(new Pair<>(leftLines, rightLines));
        assertEquals(Action.unchanged(0, 0), actions.left[0]);
        assertEquals(Action.unchanged(0, 0), actions.right[0]);
        assertEquals(Action.unchanged(1, 1), actions.left[1]);
        assertEquals(Action.unchanged(1, 1), actions.right[1]);
    }

    @Test
    void testUpdatedCode2() {
        List<String> leftLines = Arrays.asList("Same1", "Foo", "Bar", "Same1", "Same");
        List<String> rightLines = Arrays.asList("Same2", "Foo", "Bar", "Same2", "Same");
        StepDiffer d = new SeedExtendDiffer(getOptions());
        Pair<Action[]> actions = d.diffStep(new Pair<>(leftLines, rightLines));
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
