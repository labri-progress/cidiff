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
		assertEquals(DiffClient.Type.CONSOLE, Options.getClientType());
		assertEquals(LogDiffer.Algorithm.BRUTE_FORCE, Options.getAlgorithm());
		assertEquals(LogParser.Type.TRIMMING, Options.getParser());
		assertEquals(0.5, Options.getRewriteMin());
		assertEquals(false, Options.getSkipEmptyLines());
		assertEquals(0, Options.getParserDefaultTrim());
		assertEquals(false, Options.getConsoleDisplayUpdated());
		assertEquals(false, Options.getConsoleDisplayUnchanged());
		assertEquals(true, Options.getConsoleDisplayAdded());
		assertEquals(true, Options.getConsoleDisplayDeleted());
		assertEquals(true, Options.getSwingDisplaySkippedNotice());
		assertEquals("", Options.getSwingColumns());
		// check custom properties with good values
		Properties properties = new Properties();
		properties.setProperty("client", "SWING");
		properties.setProperty("differ", "SEED");
		properties.setProperty("parser", "GITHUB");
		properties.setProperty("differ.rewrite_min", "0.6");
		properties.setProperty("differ.bf.skip_empty", "true");
		properties.setProperty("parser.trimming.trim", "19");
		properties.setProperty("client.console.updated", "true");
		properties.setProperty("client.console.unchanged", "true");
		properties.setProperty("client.console.added", "false");
		properties.setProperty("client.console.deleted", "false");
		properties.setProperty("client.swing.skipped_notice", "false");
		properties.setProperty("client.swing.columns", "left");
		Options.setup(properties);
		assertEquals(DiffClient.Type.SWING, Options.getClientType());
		assertEquals(LogDiffer.Algorithm.SEED, Options.getAlgorithm());
		assertEquals(LogParser.Type.GITHUB, Options.getParser());
		assertEquals(0.6, Options.getRewriteMin());
		assertEquals(true, Options.getSkipEmptyLines());
		assertEquals(19, Options.getParserDefaultTrim());
		assertEquals(true, Options.getConsoleDisplayUpdated());
		assertEquals(true, Options.getConsoleDisplayUnchanged());
		assertEquals(false, Options.getConsoleDisplayAdded());
		assertEquals(false, Options.getConsoleDisplayDeleted());
		assertEquals(false, Options.getSwingDisplaySkippedNotice());
		assertEquals("left", Options.getSwingColumns());
		// check custom properties with wrong values throws errors
		assertThrows(IllegalArgumentException.class, () ->Options.setup(properties("client", "wrong")));
		assertThrows(IllegalArgumentException.class, () ->Options.setup(properties("differ", "wrong")));
		assertThrows(IllegalArgumentException.class, () ->Options.setup(properties("parser", "wrong")));
		assertThrows(NumberFormatException.class, () ->Options.setup(properties("differ.rewrite_min", "wrong")));
		assertThrows(NumberFormatException.class, () ->Options.setup(properties("parser.trimming.trim", "wrong")));
		// this ones should default to false with "wrong" values
		Properties wrongProperties = new Properties();
		wrongProperties.setProperty("differ.bf.skip_empty", "wrong");
		wrongProperties.setProperty("client.console.updated", "wrong");
		wrongProperties.setProperty("client.console.unchanged", "wrong");
		wrongProperties.setProperty("client.console.added", "wrong");
		wrongProperties.setProperty("client.console.deleted", "wrong");
		wrongProperties.setProperty("client.swing.skipped_notice", "wrong");
		Options.setup(wrongProperties);
		assertEquals(false, Options.getSkipEmptyLines());
		assertEquals(false, Options.getSkipEmptyLines());
		assertEquals(false, Options.getConsoleDisplayUpdated());
		assertEquals(false, Options.getConsoleDisplayUnchanged());
		assertEquals(false, Options.getConsoleDisplayAdded());
		assertEquals(false, Options.getConsoleDisplayDeleted());
		assertEquals(false, Options.getSwingDisplaySkippedNotice());
		// there is no wrong values for this option
		Options.setup(properties("client.swing.columns", "there is no wrong value for this"));
		assertEquals("there is no wrong value for this", Options.getSwingColumns());
	}

	private static Properties properties(String key, String value) {
		Properties properties = new Properties();
		properties.setProperty(key, value);
		return properties;
	}

	@Test
	void allOptions() {
		Set<String> allOptions = Options.allOptions();
		assertTrue(allOptions.contains("differ"));
		assertTrue(allOptions.contains("differ.rewrite_min"));
		assertTrue(allOptions.contains("differ.bf.skip_empty"));
		assertTrue(allOptions.contains("differ.seed.merge_seeds"));
		assertTrue(allOptions.contains("differ.seed.recursive_search"));
		assertTrue(allOptions.contains("differ.seed.even"));
		assertTrue(allOptions.contains("parser"));
		assertTrue(allOptions.contains("parser.trimming.trim"));
		assertTrue(allOptions.contains("client"));
		assertTrue(allOptions.contains("client.console.updated"));
		assertTrue(allOptions.contains("client.console.unchanged"));
		assertTrue(allOptions.contains("client.console.added"));
		assertTrue(allOptions.contains("client.console.deleted"));
		assertTrue(allOptions.contains("client.swing.skipped_notice"));
		assertTrue(allOptions.contains("client.swing.columns"));
		assertFalse(allOptions.contains("random name that should not be present"));
	}
}