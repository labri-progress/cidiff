package org.github.cidiff.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVWriter {

	public static final char SEP = ',';

	private final BufferedWriter writer;

	public CSVWriter(String filename) {
		this(filename, "");
	}

	public CSVWriter(String filename, String header) {
		try {
			File file = new File(filename);
			if (!file.getParentFile().exists() && !file.getParentFile().mkdirs())
				throw new IllegalStateException("Cannot create directories for " + file.getParentFile().getAbsolutePath());
			this.writer = new BufferedWriter(new FileWriter(file));
			this.writer.write(header);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public CSVWriter(String filename, String... headers) {
		this(filename, toCSVLine((Object[]) headers));
	}

	public static String toCSVLine(Object... elements) {
		if (elements.length > 0) {
			StringBuilder builder = new StringBuilder("\"" + elements[0] + "\"");
			for (int i = 1; i < elements.length; i++) {
				builder.append(SEP).append(elements[i]);
			}
			return builder.toString();
		}
		return "";
	}

	public CSVWriter write(Object... elements) {
		try {
			this.writer.write(toCSVLine(elements) + "\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public CSVWriter write(String str) {
		try {
			this.writer.write(str);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public void close() {
		try {
			this.writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
