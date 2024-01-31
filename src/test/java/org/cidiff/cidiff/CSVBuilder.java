package org.cidiff.cidiff;

import java.util.ArrayList;
import java.util.List;

public class CSVBuilder {

	public static final char SEP = ',';

	private final String header;
	private final List<String> lines;

	public CSVBuilder() {
		this.header = "";
		this.lines = new ArrayList<>();
	}

	public CSVBuilder(String header) {
		this.header = header;
		this.lines = new ArrayList<>();
	}

	public CSVBuilder(String... headers) {
		this.header = toCSVLine((Object[]) headers);
		this.lines = new ArrayList<>();
	}

	public static String toCSVLine(Object... elements) {
		if (elements.length > 0) {
			StringBuilder builder = new StringBuilder(String.valueOf(elements[0]));
			for (int i = 1; i < elements.length; i++) {
				builder.append(SEP).append(elements[i]);
			}
			return builder.toString();
		}
		return "";
	}

	public CSVBuilder add(Object... elements) {
		this.lines.add(toCSVLine(elements));
		return this;
	}

	public String build() {
		StringBuilder builder = new StringBuilder(this.header);
		builder.append("\n");
		for (String line : this.lines) {
			builder.append(line).append("\n");
		}
		return builder.toString();
	}
}
