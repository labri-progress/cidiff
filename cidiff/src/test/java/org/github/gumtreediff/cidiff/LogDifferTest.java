package org.github.gumtreediff.cidiff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

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

    @BeforeEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
    
    @Test
    void rewriteSimTest() {
        assertEquals(0.0, LogDiffer.rewriteSim("A B C", "A B C D"), 0.001);
        assertEquals(0.666, LogDiffer.rewriteSim("A B C", "A E C"), 0.001);
        assertEquals(0.333, LogDiffer.rewriteSim("A B C", "A E F"), 0.001);
        assertEquals(0.0, LogDiffer.rewriteSim("A B C", "G E F"), 0.001);
    }

    @Test
    void testGithubParsingMock() throws IOException {
        JobDiffer d = new JobDiffer("../data/test-gh-parser-left.csv", "../data/test-gh-parser-right.csv", DiffInputProducer.Type.GITHUB);
        assertEquals(1, d.input.leftSteps.keySet().size());
        assertTrue(d.input.leftSteps.containsKey("Set up job"));
        assertEquals(1, d.input.rightSteps.keySet().size());
        assertTrue(d.input.rightSteps.containsKey("Set up job"));
        assertEquals(4, d.input.leftSteps.get("Set up job").size());
        assertEquals(4, d.input.rightSteps.get("Set up job").size());
    }

    @Test
    void testGithubParsing() throws IOException {
        JobDiffer d = new JobDiffer("../data/1359329694.log.csv", "../data/1379038139.log.csv", DiffInputProducer.Type.GITHUB);
        assertEquals(14, d.input.leftSteps.keySet().size());
        assertTrue(d.input.leftSteps.containsKey("Set up job"));
        assertEquals(14, d.input.rightSteps.keySet().size());
        assertTrue(d.input.rightSteps.containsKey("Set up job"));
    }

    @Test
    void testClassicParsing() throws IOException {
        JobDiffer d = new JobDiffer("../data/astor_399.log.csv", "../data/astor_400.log.csv", DiffInputProducer.Type.CLASSIC);
        assertEquals(1, d.input.leftSteps.keySet().size());
        assertEquals(1, d.input.rightSteps.keySet().size());
    }
}
