package org.github.gumtreediff.cidiff;

import org.github.gumtreediff.cidiff.differs.AlternatingBruteForceLogDiffer;
import org.github.gumtreediff.cidiff.differs.SeedExtendDiffer;
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
}
