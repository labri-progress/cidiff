package org.github.gumtreediff.cidiff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.github.gumtreediff.cidiff.differs.SeedExtendDiffer;
import org.junit.jupiter.api.Test;

public class SeedExtendDifferTest {
    @Test
    void testFallback() {
        final List<LogLine> leftLines = TestHelpers.makeLog("Foo");
        final List<LogLine> rightLines = TestHelpers.makeLog("Foo");
        final LogDiffer d = new SeedExtendDiffer(TestHelpers.makeOptions(
                Options.DIFFER_SEED_BLOCK, "2", Options.DIFFER_SEED_WINDOW, "2"
        ));
        final Pair<Action[]> actions = d.diff(new Pair<>(leftLines, rightLines));
        assertEquals(Action.unchanged(0, 0), actions.left[0]);
        assertEquals(Action.unchanged(0, 0), actions.right[0]);
    }
}
