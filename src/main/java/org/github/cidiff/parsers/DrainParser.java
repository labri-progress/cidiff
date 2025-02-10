package org.github.cidiff.parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import org.github.cidiff.Drain;
import org.github.cidiff.Line;
import org.github.cidiff.LogParser;
import org.github.cidiff.Options;
import org.github.cidiff.Utils;

import static org.github.cidiff.parsers.GithubLogParser.TIMESTAMP_AND_CONTENT_REGEXP;
import static org.github.cidiff.parsers.GithubLogParser.ANSI_COLOR_REGEXP;

/**
 * Parse the log by removing the timestamp at the beginning and the ansi color codes.
 * The parser then apply the Drain log parser and replace each line by their inferred pattern.
 */
public class DrainParser implements LogParser {
	public final Drain internal;

	public DrainParser(Drain internal) {
		this.internal = internal;
	}

	@Override
	public List<Line> parse(String file, Options options) {
		final List<Line> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			int lineNumber = 0;
			for (String line; (line = br.readLine()) != null; ) {
				Matcher m = TIMESTAMP_AND_CONTENT_REGEXP.matcher(line);
				if (m.matches()) {
					// String timestamp = m.group(1) == null ? "" : m.group(1);
					String content = ANSI_COLOR_REGEXP.matcher(m.group(2)).replaceAll("");
					if (!options.skipEmptyLines() || !content.isBlank()) {
						lines.add(new Line(lineNumber, line, content));
						lineNumber++;
					}
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		this.internal.parse(lines.stream().map(Line::value).toList());
		return lines.stream()
				.map(line -> new A(line, this.internal.treeSearch(this.internal.rootNode, Arrays.asList(Utils.split(line)))))
				.map(a -> {
					if (a.cluster == null) {
						return new B(a.origin, a.origin.value());
					} else {
						return new B(a.origin, String.join(" ", a.cluster.logTemplate));
					}
				})
				.map(b -> new Line(b.origin.index(), b.origin.raw(), b.template))
				.toList();
	}

	private record A(Line origin, Drain.LogCluster cluster) {

	}

	private record B(Line origin, String template) {

	}

}
