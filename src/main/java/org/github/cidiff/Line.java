package org.github.cidiff;


import java.util.Objects;

/**
 * A line of text in a log.
 * We differentiate between the raw content, and the value of the line.
 * The value represent useful information of the line (and is usually smaller than the raw content).
 * <p>
 * An example of value could be "{@code Build completed in 10.2s}" from a raw content of "{@code 2023-11-20T15:43 Build completed in 10.2s}", where the timestamp have been filtered
 */
public class Line {

	private int index;
	private final String raw;
	private final String value;
	private final long hash;

	/**
	 * @param index the index of the line in the log file, 1-indexed
	 * @param raw   the raw content of the line (the exact string written in the file)
	 * @param value the useful information of the line
	 * @param hash  the hash of the raw content
	 */
	public Line(int index, String raw, String value, long hash) {
		this.index = index;
		this.raw = raw;
		this.value = value;
		this.hash = hash;
	}

	/**
	 * @param index the index of the line in the log file, 1-indexed
	 * @param raw   the raw content of the line (the exact string written in the file)
	 */
	public Line(int index, String raw) {
		this(index, raw, raw, raw.hashCode());
	}

	/**
	 * @param index the index of the line in the log file, 1-indexed
	 * @param raw   the raw content of the line (the exact string written in the file)
	 * @param value the useful information of the line
	 */
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
	 * @return true if the line is an empty line.
	 */
	public boolean isEmpty() {
		return this.raw.isEmpty() && this.value.isEmpty() && this.hash == 0;
	}

	public String displayValue() {
		return this.index + " " + this.value.replace("\\t", "    ");
	}

	/**
	 * @return the index of the line, 1-indexed
	 */
	public int index() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String raw() {
		return raw;
	}

	public String value() {
		return value;
	}

	public long hash() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (Line) obj;
		return this.index == that.index &&
				Objects.equals(this.raw, that.raw) &&
				Objects.equals(this.value, that.value) &&
				this.hash == that.hash;
	}

	@Override
	public int hashCode() {
		return Objects.hash(index, raw, value, hash);
	}

	@Override
	public String toString() {
		return "Line[" +
				"index=" + index + ", " +
				"raw=" + raw + ", " +
				"value=" + value + ", " +
				"hash=" + hash + ']';
	}

}
