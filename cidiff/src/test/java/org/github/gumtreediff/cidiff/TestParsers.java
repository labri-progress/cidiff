package org.github.gumtreediff.cidiff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.github.gumtreediff.cidiff.parsers.DefaultLogParser;
import org.github.gumtreediff.cidiff.parsers.FullGithubLogParser;
import org.github.gumtreediff.cidiff.parsers.RawGithubLogParser;
import org.junit.jupiter.api.Test;

public class TestParsers {
    @Test
    public void testDefaultParserWithoutTrim() throws IOException {
        final LogParser p = LogParser.get(TestHelpers.makeOptions(Options.PARSER, "DEFAULT"));
        assertEquals(DefaultLogParser.class, p.getClass());
        final List<LogLine> log = p.parse("../data/test-default-parser-1.log");
        assertEquals(2, log.size());
        assertEquals("line1", log.get(0).value);
        assertEquals(1, log.get(0).startOffset);
        assertEquals(6, log.get(0).endOffset);
        assertEquals(1, log.get(0).lineNumber);
        assertEquals("line3", log.get(1).value);
        assertEquals(3, log.get(1).lineNumber);
        assertEquals(1, log.get(1).startOffset);
        assertEquals(6, log.get(1).endOffset);
    }

    @Test
    public void testDefaultParserWithTrim() throws IOException {
        final LogParser p = LogParser.get(
                TestHelpers.makeOptions(Options.PARSER, "DEFAULT", Options.PARSER_DEFAULT_TRIM, "7"));
        assertEquals(DefaultLogParser.class, p.getClass());
        final List<LogLine> log = p.parse("../data/test-default-parser-2.log");
        assertEquals(2, log.size());
        assertEquals("line1", log.get(0).value);
        assertEquals(1, log.get(0).lineNumber);
        assertEquals(8, log.get(0).startOffset);
        assertEquals(13, log.get(0).endOffset);
        assertEquals("line3", log.get(1).value);
        assertEquals(3, log.get(1).lineNumber);
        assertEquals(8, log.get(1).startOffset);
        assertEquals(13, log.get(1).endOffset);
    }

    @Test
    public void testRawGithubParser() throws IOException {
        final LogParser p = LogParser.get(
                TestHelpers.makeOptions(Options.PARSER, "RAW_GITHUB"));
        assertEquals(RawGithubLogParser.class, p.getClass());
        final List<LogLine> log = p.parse("../data/test-raw-gh-parser-1.log");
        assertEquals(2, log.size());
        assertEquals("line1", log.get(0).value);
        assertEquals(1, log.get(0).lineNumber);
        assertEquals(30, log.get(0).startOffset);
        assertEquals(35, log.get(0).endOffset);
        assertEquals("line4", log.get(1).value);
        assertEquals(4, log.get(1).lineNumber);
        assertEquals(30, log.get(1).startOffset);
        assertEquals(35, log.get(1).endOffset);
    }

    @Test
    public void testFullGithubParser() throws IOException {
        final LogParser p = LogParser.get(
                TestHelpers.makeOptions(Options.PARSER, "FULL_GITHUB"));
        assertEquals(FullGithubLogParser.class, p.getClass());
        final List<LogLine> log = p.parse("../data/test-full-gh-parser-1.log");
        assertEquals(2, log.size());
        assertEquals("line1", log.get(0).value);
        assertEquals(1, log.get(0).lineNumber);
        assertEquals(47, log.get(0).startOffset);
        assertEquals(52, log.get(0).endOffset);
        assertEquals("line3", log.get(1).value);
        assertEquals(3, log.get(1).lineNumber);
        assertEquals(60, log.get(1).startOffset);
        assertEquals(65, log.get(1).endOffset);
    }
}
