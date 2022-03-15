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
        options.setProperty(Options.DIFFER_SEED_WINDOW, "2");
        return options;
    }

    @Test
    void testFallback() {
        List<String> leftLines = Arrays.asList("Foo");
        List<String> rightLines = Arrays.asList("Foo");
        LogDiffer d = new SeedExtendDiffer(getOptions());
        Pair<Action[]> actions = d.diff(new Pair<>(leftLines, rightLines));
        assertEquals(Action.unchanged(0, 0), actions.left[0]);
        assertEquals(Action.unchanged(0, 0), actions.right[0]);
    }

    @Test
    void testUpdatedCode() {
        List<String> leftLines = Arrays.asList("Distinct1", "Same1", "Same1", "Distinct1", "Same2", "Same2");
        List<String> rightLines = Arrays.asList("Distinct2", "Same1", "Same1", "Distinct2", "Same2", "Same2");
        LogDiffer d = new SeedExtendDiffer(getOptions());
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
