package org.github.gumtreediff.cidiff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TestFilters {
    @Test
    public void testRewriteFilter() throws IOException {
        final LogFilter f = new LogFilter.RewriteLogFilter(TestHelpers.makeOptions());
        final List<LogLine> log = TestHelpers.makeLog("foo foo foo foo", "foo bar foo foo", "line3");
        assertEquals(3, log.size());
        f.filter(log);
        assertEquals(2, log.size());
        assertEquals("foo foo foo foo", log.get(0).value);
        assertEquals("line3", log.get(1).value);
    }
}
