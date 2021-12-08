package org.github.gumtreediff.cidiff;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class OptionsTest {
    @Test
    void parseEmptyOptionsTest() {
        String[] args = {"left, right"};
        Properties options = CiDiff.parseOptions(args);
        assertEquals(0, options.size());
    }

    @Test
    void parseOneOptionTest() {
        String[] args = {"left", "right", "-o", "differ", "LCS"};
        Properties options = CiDiff.parseOptions(args);
        assertEquals(1, options.size());
        assertTrue(options.containsKey("differ"));
        assertEquals(options.get("differ"), "LCS");
    }

    @Test
    void parseTwoOptionsTest() {
        String[] args = {"left", "right", "-o", "differ", "LCS", "-o", "parser", "GITHUB"};
        Properties options = CiDiff.parseOptions(args);
        assertEquals(2, options.size());
        assertTrue(options.containsKey("differ"));
        assertEquals(options.get("differ"), "LCS");
        assertTrue(options.containsKey("parser"));
        assertEquals(options.get("parser"), "GITHUB");
    }

    @Test
    void parseIncorrectOptionsTest() {
        String[] args1 = {"left", "right", "-o", "differ"};
        assertThrows(IllegalArgumentException.class, () -> CiDiff.parseOptions(args1));
        String[] args2 = {"left", "right", "-o", "differ", "differ", "differ"};
        assertThrows(IllegalArgumentException.class, () -> CiDiff.parseOptions(args2));
        String[] args3 = {"left", "right", "-d", "differ", "differ"};
        assertThrows(IllegalArgumentException.class, () -> CiDiff.parseOptions(args3));
    }
}
