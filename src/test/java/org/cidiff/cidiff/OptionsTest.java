package org.cidiff.cidiff;

import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OptionsTest {

	@Test
	void setup() {
		// check defaults
		Options.setup(new Properties());
		Options options = Options.getInstance();
		assertEquals(DiffClient.Type.CONSOLE, options.getClientType());
		assertEquals(LogDiffer.Algorithm.BRUTE_FORCE, options.getAlgorithm());
		assertEquals(LogParser.Type.TRIMMING, options.getParser());
		assertEquals(0.5, options.getRewriteMin());
		assertEquals(false, options.getSkipEmptyLines());
		assertEquals(0, options.getParserDefaultTrim());
		assertEquals(false, options.getConsoleDisplayUpdated());
		assertEquals(false, options.getConsoleDisplayUnchanged());
		assertEquals(true, options.getConsoleDisplayAdded());
		assertEquals(true, options.getConsoleDisplayDeleted());
		assertEquals(true, options.getSwingDisplaySkippedNotice());
		assertEquals("", options.getSwingColumns());
		// check custom properties with good values
		Properties properties = new Properties();
		properties.setProperty(Options.Names.CLIENT, "SWING");
		properties.setProperty(Options.Names.DIFFER, "SEED");
		properties.setProperty(Options.Names.PARSER, "GITHUB");
		properties.setProperty(Options.Names.DIFFER_REWRITE_MIN, "0.6");
		properties.setProperty(Options.Names.DIFFER_BF_SKIP_EMPTY, "true");
		properties.setProperty(Options.Names.PARSER_DEFAULT_TRIM, "19");
		properties.setProperty(Options.Names.CONSOLE_UPDATED, "true");
		properties.setProperty(Options.Names.CONSOLE_UNCHANGED, "true");
		properties.setProperty(Options.Names.CONSOLE_ADDED, "false");
		properties.setProperty(Options.Names.CONSOLE_DELETED, "false");
		properties.setProperty(Options.Names.SWING_DISPLAY_SKIPPED_NOTICE, "false");
		properties.setProperty(Options.Names.SWING_COLUMNS, "left");
		Options.setup(properties);
		options = Options.getInstance();
		assertEquals(DiffClient.Type.SWING, options.getClientType());
		assertEquals(LogDiffer.Algorithm.SEED, options.getAlgorithm());
		assertEquals(LogParser.Type.GITHUB, options.getParser());
		assertEquals(0.6, options.getRewriteMin());
		assertEquals(true, options.getSkipEmptyLines());
		assertEquals(19, options.getParserDefaultTrim());
		assertEquals(true, options.getConsoleDisplayUpdated());
		assertEquals(true, options.getConsoleDisplayUnchanged());
		assertEquals(false, options.getConsoleDisplayAdded());
		assertEquals(false, options.getConsoleDisplayDeleted());
		assertEquals(false, options.getSwingDisplaySkippedNotice());
		assertEquals("left", options.getSwingColumns());
		// check custom properties with wrong values throws errors
		assertThrows(IllegalArgumentException.class, () ->Options.setup(properties(Options.Names.CLIENT, "wrong")));
		assertThrows(IllegalArgumentException.class, () ->Options.setup(properties(Options.Names.DIFFER, "wrong")));
		assertThrows(IllegalArgumentException.class, () ->Options.setup(properties(Options.Names.PARSER, "wrong")));
		assertThrows(NumberFormatException.class, () ->Options.setup(properties(Options.Names.DIFFER_REWRITE_MIN, "wrong")));
		assertThrows(NumberFormatException.class, () ->Options.setup(properties(Options.Names.PARSER_DEFAULT_TRIM, "wrong")));
		// this ones should default to false with "wrong" values
		Properties wrongProperties = new Properties();
		wrongProperties.setProperty(Options.Names.DIFFER_BF_SKIP_EMPTY, "wrong");
		wrongProperties.setProperty(Options.Names.CONSOLE_UPDATED, "wrong");
		wrongProperties.setProperty(Options.Names.CONSOLE_UNCHANGED, "wrong");
		wrongProperties.setProperty(Options.Names.CONSOLE_ADDED, "wrong");
		wrongProperties.setProperty(Options.Names.CONSOLE_DELETED, "wrong");
		wrongProperties.setProperty(Options.Names.SWING_DISPLAY_SKIPPED_NOTICE, "wrong");
		Options.setup(wrongProperties);
		assertEquals(false, Options.getInstance().getSkipEmptyLines());
		assertEquals(false, Options.getInstance().getSkipEmptyLines());
		assertEquals(false, Options.getInstance().getConsoleDisplayUpdated());
		assertEquals(false, Options.getInstance().getConsoleDisplayUnchanged());
		assertEquals(false, Options.getInstance().getConsoleDisplayAdded());
		assertEquals(false, Options.getInstance().getConsoleDisplayDeleted());
		assertEquals(false, Options.getInstance().getSwingDisplaySkippedNotice());
		// there is no wrong values for this option
		Options.setup(properties(Options.Names.SWING_COLUMNS, "there is no wrong value for this"));
		assertEquals("there is no wrong value for this", Options.getInstance().getSwingColumns());
	}

	private static Properties properties(String key, String value) {
		Properties properties = new Properties();
		properties.setProperty(key, value);
		return properties;
	}

	@Test
	void allOptions() {
		Set<String> allOptions = Options.allOptions();
		assertTrue(allOptions.contains(Options.Names.DIFFER));
		assertTrue(allOptions.contains(Options.Names.DIFFER_REWRITE_MIN));
		assertTrue(allOptions.contains(Options.Names.DIFFER_BF_SKIP_EMPTY));
		assertTrue(allOptions.contains(Options.Names.PARSER));
		assertTrue(allOptions.contains(Options.Names.PARSER_DEFAULT_TRIM));
		assertTrue(allOptions.contains(Options.Names.CLIENT));
		assertTrue(allOptions.contains(Options.Names.CONSOLE_UPDATED));
		assertTrue(allOptions.contains(Options.Names.CONSOLE_UNCHANGED));
		assertTrue(allOptions.contains(Options.Names.CONSOLE_ADDED));
		assertTrue(allOptions.contains(Options.Names.CONSOLE_DELETED));
		assertTrue(allOptions.contains(Options.Names.SWING_DISPLAY_SKIPPED_NOTICE));
		assertTrue(allOptions.contains(Options.Names.SWING_COLUMNS));
		assertFalse(allOptions.contains("random name that should not be present"));
	}
}