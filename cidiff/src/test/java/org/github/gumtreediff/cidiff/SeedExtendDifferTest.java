package org.github.gumtreediff.cidiff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.github.gumtreediff.cidiff.differs.SeedExtendDiffer;
import org.junit.jupiter.api.Test;

public class SeedExtendDifferTest {
    @Test
    void testFallback() {
        final var leftLog = TestHelpers.makeLog("Foo");
        final var rightLog = TestHelpers.makeLog("Foo");
        final var d = new SeedExtendDiffer(TestHelpers.makeOptions(
                Options.DIFFER_SEED_BLOCK, "2", Options.DIFFER_SEED_WINDOW, "2"
        ));
        final var actions = d.diff(new Pair<>(leftLog, rightLog));
        assertEquals(Action.unchanged(leftLog.get(0), rightLog.get(0)), actions.left.get(leftLog.get(0)));
        assertEquals(Action.unchanged(leftLog.get(0), rightLog.get(0)), actions.right.get(rightLog.get(0)));
    }
}
