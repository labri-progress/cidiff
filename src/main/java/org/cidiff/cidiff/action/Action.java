package org.cidiff.cidiff.action;

import org.cidiff.cidiff.Line;

public abstract class Action {
	public static final Action EMPTY = new Action(Line.EMPTY, Line.EMPTY, Type.NONE) {
	};
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

	/**
	 * @return true if the line is {@link #EMPTY}.
	 */
	public boolean isEmpty() {
		return this == EMPTY;
	}

//	abstract Action toPlaceholder();

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
