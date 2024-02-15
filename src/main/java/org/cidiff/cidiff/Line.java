package org.cidiff.cidiff;


/**
 * A line of text in a log.
 * We differentiate between the raw content, and the value of the line.
 * The value represent useful information of the line (and is usually smaller than the raw content).
 * <p>
 * An example of value could be "{@code Build completed in 10.2s}" from a raw content of "{@code 2023-11-20T15:43 Build completed in 10.2s}", where the timestamp have been filtered
 *
 * @param index the index of the line in the log file, 1-indexed
 * @param raw   the raw content of the line (the exact string written in the file)
 * @param value the useful information of the line
 * @param hash  the hash of the raw content
 */
public record Line(int index, String raw, String value, long hash) {

	public static final Line EMPTY = new Line(-1, "", "", 0);

	public Line(int index, String raw) {
		this(index, raw, raw, raw.hashCode());
	}

	public Line(int index, String raw, String value) {
		this(index, raw, value, raw.hashCode());
	}

	/**
	 * Determine if two lines have the same value
	 *
	 * @param line the other line
	 * @return true if the values of the lines are equals
	 */
	public boolean hasSameValue(Line line) {
		return value().equals(line.value());
	}

	/**
	 * @return true if the line is {@link #EMPTY}.
	 */
	public boolean isEmpty() {
		return this == EMPTY;
	}

	public String displayValue() {
		return (this.index-1) + " " + this.value.replace("\\t", "    ");
	}

}
