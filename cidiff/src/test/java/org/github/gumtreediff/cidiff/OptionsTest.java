package org.github.gumtreediff.cidiff;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

import org.junit.jupiter.api.Test;

public class OptionsTest {
    @Test
    void parseEmptyOptionsTest() {
        final String[] args = {"left, right"};
        final Properties options = CiDiff.parseOptions(args);
        assertEquals(0, options.size());
    }

    @Test
    void parseOneOptionTest() {
        final String[] args = {"left", "right", "-o", "differ", "LCS"};
        final Properties options = CiDiff.parseOptions(args);
        assertEquals(1, options.size());
        assertTrue(options.containsKey("differ"));
        assertEquals(options.get("differ"), "LCS");
    }

    @Test
    void parseTwoOptionsTest() {
        final String[] args = {"left", "right", "-o", "differ", "LCS", "-o", "parser", "GITHUB"};
        final Properties options = CiDiff.parseOptions(args);
        assertEquals(2, options.size());
        assertTrue(options.containsKey("differ"));
        assertEquals(options.get("differ"), "LCS");
        assertTrue(options.containsKey("parser"));
        assertEquals(options.get("parser"), "GITHUB");
    }

    @Test
    void parseIncorrectOptionsTest() {
        final String[] args1 = {"left", "right", "-o", "differ"};
        assertThrows(IllegalArgumentException.class, () -> CiDiff.parseOptions(args1));
        final String[] args2 = {"left", "right", "-o", "differ", "differ", "differ"};
        assertThrows(IllegalArgumentException.class, () -> CiDiff.parseOptions(args2));
        final String[] args3 = {"left", "right", "-d", "differ", "differ"};
        assertThrows(IllegalArgumentException.class, () -> CiDiff.parseOptions(args3));
    }
}
