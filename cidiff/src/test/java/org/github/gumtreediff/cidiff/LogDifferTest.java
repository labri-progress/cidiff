package org.github.gumtreediff.cidiff;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    void rewriteSimTest() {
        assertEquals(0.0, StepDiffer.rewriteSim("A B C", "A B C D"), 0.001);
        assertEquals(0.666, StepDiffer.rewriteSim("A B C", "A E C"), 0.001);
        assertEquals(0.333, StepDiffer.rewriteSim("A B C", "A E F"), 0.001);
        assertEquals(0.0, StepDiffer.rewriteSim("A B C", "G E F"), 0.001);
    }

    @Test
    void testGithubParsingMock() throws IOException {
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
    void testDiffer() throws IOException {
        final Properties options = new Properties();
        options.setProperty("parser", "GITHUB");
        LogDiffer d = new LogDiffer("../data/test-gh-parser-left.csv", "../data/test-gh-parser-right.csv", options);
        System.out.println("yup");
        System.out.println(outContent.toString());
        assertTrue(outContent.toString().contains("D[3] Foo"));
        assertTrue(outContent.toString().contains("A[4] Bar"));
    }

    @Test
    void testGithubParsing() throws IOException {
        final Properties options = new Properties();
        options.setProperty("parser", "GITHUB");
        LogDiffer d = new LogDiffer("../data/1359329694.log.csv", "../data/1379038139.log.csv", options);
        assertEquals(14, d.parser.leftSteps.keySet().size());
        assertTrue(d.parser.leftSteps.containsKey("Set up job"));
        assertEquals(14, d.parser.rightSteps.keySet().size());
        assertTrue(d.parser.rightSteps.containsKey("Set up job"));
    }

    @Test
    void testClassicParsing() throws IOException {
        final Properties options = new Properties();
        options.setProperty("parser", "DEFAULT");
        LogDiffer d = new LogDiffer("../data/astor_399.log.csv", "../data/astor_400.log.csv", options);
        assertEquals(1, d.parser.leftSteps.keySet().size());
        assertEquals(1, d.parser.rightSteps.keySet().size());
    }
}
