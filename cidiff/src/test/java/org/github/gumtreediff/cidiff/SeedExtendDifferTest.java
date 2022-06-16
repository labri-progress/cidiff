package org.github.gumtreediff.cidiff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.github.gumtreediff.cidiff.differs.SeedExtendDiffer;
import org.junit.jupiter.api.Test;

public class SeedExtendDifferTest {
    private static Properties getOptions() {
        final Properties options = new Properties();
        options.setProperty(Options.DIFFER_SEED_BLOCK, "2");
        options.setProperty(Options.DIFFER_SEED_WINDOW, "2");
        return options;
    }

    @Test
    void testFallback() {
        final List<String> leftLines = Arrays.asList("Foo");
        final List<String> rightLines = Arrays.asList("Foo");
        final LogDiffer d = new SeedExtendDiffer(getOptions());
        final Pair<Action[]> actions = d.diff(new Pair<>(leftLines, rightLines));
        assertEquals(Action.unchanged(0, 0), actions.left[0]);
        assertEquals(Action.unchanged(0, 0), actions.right[0]);
    }
}
