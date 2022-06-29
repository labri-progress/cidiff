package org.github.gumtreediff.cidiff;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.github.gumtreediff.cidiff.clients.ConsoleClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsoleClientTest {
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
    void testOutput() {
        final DiffClient d = new ConsoleClient("../data/test-gh-parser-left.log",
                "../data/test-gh-parser-right.log",
                TestHelpers.makeOptions(Options.PARSER, "RAW_GITHUB", Options.CONSOLE_UPDATED, "true"));

        d.execute();
        assertTrue(outContent.toString().contains("- 3 Fooo"));
        assertTrue(outContent.toString().contains("+ 4 Bar"));
        assertTrue(outContent.toString().contains("> 2 Current runner version:"));
        assertTrue(outContent.toString().contains("Current runner version:"));
    }
}
