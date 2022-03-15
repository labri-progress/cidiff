package org.github.gumtreediff.cidiff;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

class LogDifferCliTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testGithubParsingMock() {
        final Properties options = new Properties();
        options.setProperty(Options.PARSER, "RAW_GITHUB");
        LogDifferCli d = new LogDifferCli("../data/test-gh-parser-left.csv", "../data/test-gh-parser-right.csv", options);
        assertEquals(4, d.parser.lines.left.size());
        assertEquals(4, d.parser.lines.right.size());
        assertEquals(4, d.parser.lines.left.size());
        assertEquals("Fooo", d.parser.lines.left.get(2));
        assertEquals("Bar", d.parser.lines.right.get(3));
    }

    @Test
    void testOutput() {
        final Properties options = new Properties();
        options.setProperty(Options.PARSER, "RAW_GITHUB");
        options.setProperty(Options.DIFFER_UPDATED, "true");
        LogDifferCli d = new LogDifferCli("../data/test-gh-parser-left.csv", "../data/test-gh-parser-right.csv", options);
        assertTrue(outContent.toString().contains("- 3 Fooo"));
        assertTrue(outContent.toString().contains("+ 4 Bar"));
        assertTrue(outContent.toString().contains("> 2 Current runner version: '2.283.2'"));
        assertTrue(outContent.toString().contains("Current runner version: '2.283.3'"));
        assertEquals(1, d.getMetrics().added);
        assertEquals(1, d.getMetrics().deleted);
        assertEquals(1, d.getMetrics().updated);
        assertEquals(2, d.getMetrics().unchanged);
    }

    @Test
    void testGithubParsing() {
        final Properties options = new Properties();
        options.setProperty(Options.PARSER, "FULL_GITHUB");
        LogDifferCli d = new LogDifferCli("../data/gumtree_191.log.csv", "../data/gumtree_192.log.csv", options);
        //FIXME
    }

    @Test
    void testClassicParsing() {
        final Properties options = new Properties();
        options.setProperty(Options.PARSER, "DEFAULT");
        LogDifferCli d = new LogDifferCli("../data/astor_399.log.csv", "../data/astor_400.log.csv", options);
        //FIXME
    }

    @Test
    void testClassicParsingWithTimestamp() {
        final Properties options = new Properties();
        options.setProperty(Options.PARSER_DEFAULT_TRIM, "29");
        options.setProperty(Options.PARSER, "DEFAULT");
        LogDifferCli d = new LogDifferCli("../data/budibase_left.log.csv", "../data/budibase_right.log.csv", options);
        assertEquals("Found online and idle hosted runner in the current repository's organization account that matches the required labels: 'ubuntu-latest'", d.parser.lines.left.get(0));
    }
}
