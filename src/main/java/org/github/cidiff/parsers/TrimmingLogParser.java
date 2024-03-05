package org.github.cidiff.parsers;


import org.github.cidiff.Line;
import org.github.cidiff.LogParser;
import org.github.cidiff.Options;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Trim each line according to the set option (default is no trim).
 */
public final class TrimmingLogParser implements LogParser {

	public final int trim = Options.getParserDefaultTrim();

	public List<Line> parse(String file) {
		final List<Line> log = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			int lineNumber = 0;
			for (String line; (line = br.readLine()) != null; ) {
				lineNumber++;
				final String value = line.substring(Math.min(trim, line.length()));
				log.add(new Line(lineNumber, line, value));
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

		return log;
	}
}
