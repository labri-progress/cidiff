package org.github.gumtreediff.cidiff;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.github.gumtreediff.cidiff.benchmarks.PrecisionBenchmark;
import org.junit.jupiter.api.Test;

public class TestPrecisionBenchmark {
    @Test
    public void testConsecutiveAddIntervals() {
        final List<LogLine> log = TestHelpers.makeLog("line1", "line2", "line3", "line4");
        log.remove(1);
        final Action[] actions = TestHelpers.makeActions(Action.Type.UNCHANGED, Action.Type.ADDED, Action.Type.ADDED);
        final List<int[]> intervals = PrecisionBenchmark.getIntervalsCiDiff(actions, log);
        assertEquals(1, intervals.size());
        assertArrayEquals(new int[] {3, 5}, intervals.get(0));
    }

    @Test
    public void testLastAddWithoutGapIntervals() {
        final List<LogLine> log = TestHelpers.makeLog("line1", "line2");
        final Action[] actions = TestHelpers.makeActions(Action.Type.UNCHANGED, Action.Type.ADDED);
        final List<int[]> intervals = PrecisionBenchmark.getIntervalsCiDiff(actions, log);
        assertEquals(1, intervals.size());
        assertArrayEquals(new int[] {2, 3}, intervals.get(0));
    }

    @Test
    public void testLastAddWithGapIntervals() {
        final List<LogLine> log = TestHelpers.makeLog("line1", "line2", "line3", "line4");
        log.remove(2);
        final Action[] actions = TestHelpers.makeActions(Action.Type.UNCHANGED, Action.Type.ADDED, Action.Type.ADDED);
        final List<int[]> intervals = PrecisionBenchmark.getIntervalsCiDiff(actions, log);
        assertEquals(2, intervals.size());
        assertArrayEquals(new int[] {2, 3}, intervals.get(0));
        assertArrayEquals(new int[] {4, 5}, intervals.get(1));
    }

    @Test
    public void testInitialAddIntervals() {
        final List<LogLine> log = TestHelpers.makeLog("line1", "line2");
        final Action[] actions = TestHelpers.makeActions(Action.Type.ADDED, Action.Type.UNCHANGED);
        final List<int[]> intervals = PrecisionBenchmark.getIntervalsCiDiff(actions, log);
        assertEquals(1, intervals.size());
        assertArrayEquals(new int[] {1, 2}, intervals.get(0));
    }

    @Test
    public void testTwoInitialAddsIntervals() {
        final List<LogLine> log = TestHelpers.makeLog("line1", "line2", "line3");
        final Action[] actions = TestHelpers.makeActions(Action.Type.ADDED, Action.Type.ADDED, Action.Type.UNCHANGED);
        final List<int[]> intervals = PrecisionBenchmark.getIntervalsCiDiff(actions, log);
        assertEquals(1, intervals.size());
        assertArrayEquals(new int[] {1, 3}, intervals.get(0));
    }
}
