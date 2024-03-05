package org.cidiff.cidiff.parsers;


import org.cidiff.cidiff.Line;
import org.cidiff.cidiff.LogParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Removes timestamps at the beginning (if present) and ansi color codes.
 */
public final class GithubLogParser implements LogParser {

	private static final Pattern TIMESTAMP_AND_CONTENT_REGEXP = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{7}Z ?)?(.*)");
	private static final Pattern ANSI_COLOR_REGEXP = Pattern.compile("\\e?\\[(\\d\\d?)?(;\\d\\d?)*m");

	public List<Line> parse(String file) {
		final List<Line> log = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			int lineNumber = 0;
			for (String line; (line = br.readLine()) != null; ) {
				lineNumber++;
				Matcher m = TIMESTAMP_AND_CONTENT_REGEXP.matcher(line);
				if (m.matches()) {
//					String timestamp = m.group(1) == null ? "" : m.group(1);
					String content = ANSI_COLOR_REGEXP.matcher(m.group(2)).replaceAll("");
					log.add(new Line(lineNumber, line, content));
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

		return log;
	}
}
