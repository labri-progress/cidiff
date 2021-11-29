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
        assertEquals(0.0, JobDiffer.rewriteSim("A B C", "A B C D"), 0.001);
        assertEquals(0.666, JobDiffer.rewriteSim("A B C", "A E C"), 0.001);
        assertEquals(0.333, JobDiffer.rewriteSim("A B C", "A E F"), 0.001);
        assertEquals(0.0, JobDiffer.rewriteSim("A B C", "G E F"), 0.001);
    }

    @Test
    void testParsing() throws IOException {
        JobDiffer d = new JobDiffer("../data/test-gh-parser-left.csv", "../data/test-gh-parser-right.csv");
        d.launch();
        assertEquals(1, d.leftSteps.keySet().size());
        assertTrue(d.leftSteps.containsKey("Set up job"));
        assertEquals(1, d.rightSteps.keySet().size());
        assertTrue(d.rightSteps.containsKey("Set up job"));
        assertEquals("Current runner version: '2.283.2'", d.leftSteps.get("Set up job").get(0));
    }

    @Test
    void testPruneSeeds() throws IOException {
        JobDiffer d = new JobDiffer("../data/test-gh-parser-left.csv", "../data/test-gh-parser-right.csv");
        d.launch();
        assertEquals(2, d.leftSteps.get("Set up job").size());
        assertEquals(2, d.rightSteps.get("Set up job").size());
    }
}
