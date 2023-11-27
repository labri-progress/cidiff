package org.cidiff.cidiff;

import java.util.Objects;

public final class Action {

	public static final Action EMPTY = new Action(Line.EMPTY, Line.EMPTY, Type.NONE);
	public final Line leftLogLine;
	public final Line rightLogLine;
	public final Type type;
	public double sim = 0D;
	public int debug = 0;

	public Action(Line leftLogLine, Line rightLogLine, Type type) {
		this.leftLogLine = leftLogLine;
		this.rightLogLine = rightLogLine;
		this.type = type;
	}

	public static Action added(Line rightLogLine) {
		return new Action(null, rightLogLine, Type.ADDED);
	}

	public static Action deleted(Line leftLogLine) {
		return new Action(leftLogLine, null, Type.DELETED);
	}

	public static Action unchanged(Line leftLogLine, Line rightLogLine) {
		return new Action(leftLogLine, rightLogLine, Type.UNCHANGED);
	}

	public static Action updated(Line leftLogLine, Line rightLogLine) {
		return new Action(leftLogLine, rightLogLine, Type.UPDATED);
	}

	public static Action skipped(Line line) {
		return new Action(line, null, Type.SKIPPED);
	}

	public static Action moved(Action action) {
		if (action.type == Type.UNCHANGED) {
			return new Action(action.leftLogLine, action.rightLogLine, Type.MOVED_UNCHANGED);
		} else if (action.type == Type.UPDATED) {
			return new Action(action.leftLogLine, action.rightLogLine, Type.MOVED_UPDATED);
		} else {
			// this should never happen
			return action;
		}
	}

	public Action withDebug(int deb) { /* I want to name it debug, but checkstyle doesn't want*/
		this.debug = deb;
		return this;
	}

	/**
	 * @return true if the line is {@link #EMPTY}.
	 */
	public boolean isEmpty() {
		return this == EMPTY;
	}

	@Override
	public String toString() {
		if (leftLogLine == null)
			return type + " [" + rightLogLine.index() + "]";
		else if (rightLogLine == null)
			return type + " [" + leftLogLine.index() + "]";
		else
			return type + " [" + leftLogLine.index() + "-" + rightLogLine.index() + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final Action action = (Action) o;
		return leftLogLine == action.leftLogLine
				&& rightLogLine == action.rightLogLine
				&& type == action.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(leftLogLine, rightLogLine, type);
	}

	public enum Type {
		ADDED,
		DELETED,
		UPDATED,
		UNCHANGED,
		SKIPPED,
		MOVED_UNCHANGED,
		MOVED_UPDATED,
		NONE;
	}
}
