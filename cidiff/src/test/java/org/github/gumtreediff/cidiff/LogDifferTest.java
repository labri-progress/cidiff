package org.github.gumtreediff.cidiff;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

class LogDifferTest {
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
        options.setProperty("parser", "GITHUB");
        LogDiffer d = new LogDiffer("../data/test-gh-parser-left.csv", "../data/test-gh-parser-right.csv", options);
        assertEquals(1, d.parser.leftSteps.keySet().size());
        assertTrue(d.parser.leftSteps.containsKey("Set up job"));
        assertEquals(1, d.parser.rightSteps.keySet().size());
        assertTrue(d.parser.rightSteps.containsKey("Set up job"));
        assertEquals(4, d.parser.leftSteps.get("Set up job").size());
        assertEquals(4, d.parser.rightSteps.get("Set up job").size());
    }

    @Test
    void testOutput() {
        final Properties options = new Properties();
        options.setProperty(Options.PARSER, "GITHUB");
        options.setProperty(Options.DIFFER_UPDATED, "true");
        LogDiffer d = new LogDiffer("../data/test-gh-parser-left.csv", "../data/test-gh-parser-right.csv", options);
        System.out.println(outContent);
        assertTrue(outContent.toString().contains("- 3 Fooo"));
        assertTrue(outContent.toString().contains("+ 4 Bar"));
        assertTrue(outContent.toString().contains("> 2 Current runner version: '2.283.2'"));
        assertTrue(outContent.toString().contains("Current runner version: '2.283.3'"));
    }

    @Test
    void testGithubParsing() {
        final Properties options = new Properties();
        options.setProperty("parser", "GITHUB");
        LogDiffer d = new LogDiffer("../data/gumtree_191.log.csv", "../data/gumtree_192.log.csv", options);
        assertEquals(14, d.parser.leftSteps.keySet().size());
        assertTrue(d.parser.leftSteps.containsKey("Set up job"));
        assertEquals(14, d.parser.rightSteps.keySet().size());
        assertTrue(d.parser.rightSteps.containsKey("Set up job"));
    }

    @Test
    void testClassicParsing() {
        final Properties options = new Properties();
        options.setProperty("parser", "DEFAULT");
        LogDiffer d = new LogDiffer("../data/astor_399.log.csv", "../data/astor_400.log.csv", options);
        assertEquals(1, d.parser.leftSteps.keySet().size());
        assertEquals(1, d.parser.rightSteps.keySet().size());
    }
}
