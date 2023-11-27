package org.cidiff.cidiff.parsers;


import org.cidiff.cidiff.Line;
import org.cidiff.cidiff.LogParser;
import org.cidiff.cidiff.Options;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Trim each line according to the set option (default is no trim).
 */
public final class TrimmingLogParser implements LogParser {

	public final int trim = Options.getInstance().getParserDefaultTrim();

	public List<Line> parse(String file) {
		final List<Line> log = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			int lineNumber = 0;
			int relativeIndex = 0;
			for (String line; (line = br.readLine()) != null; ) {
				lineNumber++;
				if (line.length() > trim) {
					final String value = line.substring(trim);
					log.add(new Line(lineNumber, line, value));
					relativeIndex++;
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

		return log;
	}
}
