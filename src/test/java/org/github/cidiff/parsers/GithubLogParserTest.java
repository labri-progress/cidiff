package org.github.cidiff.parsers;

import org.github.cidiff.Line;
import org.github.cidiff.Options;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GithubLogParserTest {

	@Test
	void parse() {
		Options options = new Options();
		GithubLogParser parserTrim0 = new GithubLogParser();
		List<Line> parsed = parserTrim0.parse(GithubLogParserTest.class.getClassLoader().getResource("test-parser-github.log").getPath(), options);
		assertEquals(6, parsed.size());
		assertEquals("2021-10-19T13:45:43.6326799Z ##[debug]Starting: Set up job", parsed.get(0).raw());
		assertEquals("##[debug]Starting: Set up job", parsed.get(0).value());
		assertEquals(1, parsed.get(0).index());
		assertEquals("[\u001B[1;34mINFO\u001B[m] Scanning for projects...", parsed.get(3).raw());
		assertEquals("[INFO] Scanning for projects...", parsed.get(3).value());
		assertEquals(4, parsed.get(3).index());
		assertEquals("[\u001B[1;34mINFO\u001B[m] \u001B[1m--- \u001B[0;32mmaven-clean-plugin:3.1.0:clean\u001B[m \u001B[1m(default-clean)\u001B[m @ \u001B[36massertj-core\u001B[0;1m ---\u001B[m", parsed.get(4).raw());
		assertEquals("[INFO] --- maven-clean-plugin:3.1.0:clean (default-clean) @ assertj-core ---", parsed.get(4).value());
		assertEquals(5, parsed.get(4).index());
		assertEquals("WARNING: All illegal access operations will be denied in a future release", parsed.get(5).raw());
		assertEquals("WARNING: All illegal access operations will be denied in a future release", parsed.get(5).value());
		assertEquals(6, parsed.get(5).index());
	}
}