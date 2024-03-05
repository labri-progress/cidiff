package org.github.cidiff;

public record Action(Line left, Line right, Type type, double sim) {

	public static final Action NONE = new Action(null, null, Type.NONE);

	public Action(Line left, Line right, Type type) {
		this(left, right, type, 0);
	}

	public static Action added(Line right) {
		return new Action(null, right, Type.ADDED);
	}

	public static Action deleted(Line left) {
		return new Action(left, null, Type.DELETED);
	}

	public static Action unchanged(Line left, Line right, double sim) {
		return new Action(left, right, Type.UNCHANGED, sim);
	}

	public static Action updated(Line left, Line right, double sim) {
		return new Action(left, right, Type.UPDATED, sim);
	}

	public static Action skipped(Line line) {
		return new Action(line, null, Type.SKIPPED);
	}

	public static Action moved(Action action) {
		if (action.type == Type.UNCHANGED) {
			return new Action(action.left, action.right, Type.MOVED_UNCHANGED);
		} else if (action.type == Type.UPDATED) {
			return new Action(action.left, action.right, Type.MOVED_UPDATED);
		} else {
			// this should never happen
			return action;
		}
	}

	/**
	 * @return true if the line is {@link #NONE}.
	 */
	public boolean isNone() {
		return this == NONE;
	}

	@Override
	public String toString() {
		if (left == null)
			return type + " [" + right.index() + "]";
		else if (right == null)
			return type + " [" + left.index() + "]";
		else
			return type + " [" + left.index() + "-" + right.index() + "]";
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
