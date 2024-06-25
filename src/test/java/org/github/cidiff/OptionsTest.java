package org.github.cidiff;

import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OptionsTest {

	@Test
	void setup() {
		// check defaults
		Options opt = new Options();
		assertEquals(DiffClient.Type.CONSOLE, opt.clientType());
		assertEquals(LogDiffer.Algorithm.SEED, opt.algorithm());
		assertEquals(LogParser.Type.TRIMMING, opt.parser());
		assertEquals(0.5, opt.rewriteMin());
		assertEquals(false, opt.skipEmptyLines());
		assertEquals(0, opt.parserDefaultTrim());
		assertEquals(false, opt.consoleDisplayUpdated());
		assertEquals(false, opt.consoleDisplayUnchanged());
		assertEquals(true, opt.consoleDisplayAdded());
		assertEquals(false, opt.consoleDisplayDeleted());
		assertEquals(true, opt.swingDisplaySkippedNotice());
		assertEquals("", opt.swingColumns());
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
		Options fromProperties = Options.from(properties);
		assertEquals(DiffClient.Type.SWING, fromProperties.clientType());
		assertEquals(LogDiffer.Algorithm.SEED, fromProperties.algorithm());
		assertEquals(LogParser.Type.GITHUB, fromProperties.parser());
		assertEquals(0.6, fromProperties.rewriteMin());
		assertEquals(true, fromProperties.skipEmptyLines());
		assertEquals(19, fromProperties.parserDefaultTrim());
		assertEquals(true, fromProperties.consoleDisplayUpdated());
		assertEquals(true, fromProperties.consoleDisplayUnchanged());
		assertEquals(false, fromProperties.consoleDisplayAdded());
		assertEquals(false, fromProperties.consoleDisplayDeleted());
		assertEquals(false, fromProperties.swingDisplaySkippedNotice());
		assertEquals("left", fromProperties.swingColumns());
		// check custom properties with wrong values throws errors
		assertThrows(IllegalArgumentException.class, () ->Options.from(properties("client", "wrong")));
		assertThrows(IllegalArgumentException.class, () ->Options.from(properties("differ", "wrong")));
		assertThrows(IllegalArgumentException.class, () ->Options.from(properties("parser", "wrong")));
		assertThrows(NumberFormatException.class, () ->Options.from(properties("differ.rewrite_min", "wrong")));
		assertThrows(NumberFormatException.class, () ->Options.from(properties("parser.trimming.trim", "wrong")));
		// this ones should default to false with "wrong" values
		Properties wrongProperties = new Properties();
		wrongProperties.setProperty("differ.bf.skip_empty", "wrong");
		wrongProperties.setProperty("client.console.updated", "wrong");
		wrongProperties.setProperty("client.console.unchanged", "wrong");
		wrongProperties.setProperty("client.console.added", "wrong");
		wrongProperties.setProperty("client.console.deleted", "wrong");
		wrongProperties.setProperty("client.swing.skipped_notice", "wrong");
		Options fromWrongProperties = Options.from(wrongProperties);
		assertEquals(false, fromWrongProperties.skipEmptyLines());
		assertEquals(false, fromWrongProperties.skipEmptyLines());
		assertEquals(false, fromWrongProperties.consoleDisplayUpdated());
		assertEquals(false, fromWrongProperties.consoleDisplayUnchanged());
		assertEquals(false, fromWrongProperties.consoleDisplayAdded());
		assertEquals(false, fromWrongProperties.consoleDisplayDeleted());
		assertEquals(false, fromWrongProperties.swingDisplaySkippedNotice());
		// there is no wrong values for this option
		Options options = Options.from(properties("client.swing.columns", "there is no wrong value for this"));
		assertEquals("there is no wrong value for this", options.swingColumns());
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