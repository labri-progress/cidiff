package org.github.cidiff.parsers;

import org.github.cidiff.Line;
import org.github.cidiff.Options;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class TrimmingLogParserTest {

	private Properties properties(String key, String value) {
		Properties properties = new Properties();
		properties.setProperty(key, value);
		return properties;
	}

	@Test
	void parse() {
		// default, no trim
		Options options = new Options();
		TrimmingLogParser parserTrim0 = new TrimmingLogParser();
		List<Line> parsed = parserTrim0.parse(TrimmingLogParserTest.class.getClassLoader().getResource("test-parser-trim.log").getPath(), options);
		assertEquals(3, parsed.size());
		assertEquals("line 1", parsed.get(0).raw());
		assertEquals("line 1", parsed.get(0).value());
		assertEquals(0, parsed.get(0).index());
		assertEquals("", parsed.get(1).raw());
		assertEquals("", parsed.get(1).value());
		assertEquals(1, parsed.get(1).index());
		assertEquals("line 2", parsed.get(2).raw());
		assertEquals("line 2", parsed.get(2).value());
		assertEquals(2, parsed.get(2).index());

		// trim 2 characters
		Options options1 = new Options().with(Options.DEFAULT_TRIM, 2);
		TrimmingLogParser parserTrim2 = new TrimmingLogParser();
		List<Line> parsed1 = parserTrim2.parse(TrimmingLogParserTest.class.getClassLoader().getResource("test-parser-trim.log").getPath(), options1);
		assertEquals(3, parsed1.size());
		assertEquals("line 1", parsed1.get(0).raw());
		assertEquals("ne 1", parsed1.get(0).value());
		assertEquals(0, parsed1.get(0).index());
		assertEquals("", parsed.get(1).raw());
		assertEquals("", parsed.get(1).value());
		assertEquals(1, parsed.get(1).index());
		assertEquals("line 2", parsed1.get(2).raw());
		assertEquals("ne 2", parsed1.get(2).value());
		assertEquals(2, parsed1.get(2).index());


		// trim all characters
		Options options2 = new Options().with(Options.DEFAULT_TRIM, 50);
		TrimmingLogParser parserTrim50 = new TrimmingLogParser();
		List<Line> parsed2 = parserTrim50.parse(TrimmingLogParserTest.class.getClassLoader().getResource("test-parser-trim.log").getPath(), options2);
		assertEquals(3, parsed2.size());
		assertEquals("line 1", parsed2.get(0).raw());
		assertEquals("", parsed2.get(0).value());
		assertEquals(0, parsed2.get(0).index());
		assertEquals("", parsed2.get(1).raw());
		assertEquals("", parsed2.get(1).value());
		assertEquals(1, parsed2.get(1).index());
		assertEquals("line 2", parsed2.get(2).raw());
		assertEquals("", parsed2.get(2).value());
		assertEquals(2, parsed2.get(2).index());
	}
}