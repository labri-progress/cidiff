package org.cidiff.cidiff.parsers;

import org.cidiff.cidiff.Line;
import org.cidiff.cidiff.Options;
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
		Options.setup(new Properties());
		TrimmingLogParser parserTrim0 = new TrimmingLogParser();
		List<Line> parsed = parserTrim0.parse(TrimmingLogParserTest.class.getClassLoader().getResource("test-parser-trim.log").getPath());
		assertEquals(2, parsed.size());
		assertEquals("line 1", parsed.get(0).raw());
		assertEquals("line 1", parsed.get(0).value());
		assertEquals(1, parsed.get(0).index());
		assertEquals("line 2", parsed.get(1).raw());
		assertEquals("line 2", parsed.get(1).value());
		assertEquals(3, parsed.get(1).index());

		// trim 2 characters
		Options.setup(properties(Options.Names.PARSER_DEFAULT_TRIM, "2"));
		TrimmingLogParser parserTrim2 = new TrimmingLogParser();
		List<Line> parsed1 = parserTrim2.parse(TrimmingLogParserTest.class.getClassLoader().getResource("test-parser-trim.log").getPath());
		assertEquals(2, parsed1.size());
		assertEquals("line 1", parsed1.get(0).raw());
		assertEquals("ne 1", parsed1.get(0).value());
		assertEquals(1, parsed1.get(0).index());
		assertEquals("line 2", parsed1.get(1).raw());
		assertEquals("ne 2", parsed1.get(1).value());
		assertEquals(3, parsed1.get(1).index());


		// trim all characters
		Options.setup(properties(Options.Names.PARSER_DEFAULT_TRIM, "50"));
		TrimmingLogParser parserTrim10 = new TrimmingLogParser();
		List<Line> parsed2 = parserTrim10.parse(TrimmingLogParserTest.class.getClassLoader().getResource("test-parser-trim.log").getPath());
		assertEquals(0, parsed2.size());
	}
}