package org.github.cidiff;

import org.github.cidiff.parsers.GithubLogParser;
import org.github.cidiff.parsers.TrimmingLogParser;

import java.util.List;
import java.util.function.Supplier;

/**
 * Abstract class for log parsers.
 * The parse method of log parsers must be stateless.
 */
public interface LogParser {

	List<Line> parse(String file, Options options);

	enum Type {
		TRIMMING(TrimmingLogParser::new),
		GITHUB(GithubLogParser::new);

		private final Supplier<LogParser> constructor;

		Type(Supplier<LogParser> constructor) {
			this.constructor = constructor;
		}

		public LogParser construct() {
			return this.constructor.get();
		}
	}
}
