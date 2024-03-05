package org.github.cidiff.clients;

import org.github.cidiff.Action;
import org.github.cidiff.Line;
import org.github.cidiff.Pair;

import java.util.ArrayList;
import java.util.List;

public final class JsonClient extends AbstractDiffClient {

	public JsonClient(Pair<List<Line>> lines, Pair<List<Action>> actions) {
		super(lines, actions);
	}

	public void execute() {
		StringBuilder sb = new StringBuilder("[");

		// deduplicate the list of actions
		ArrayList<Action> actionList = new ArrayList<>(actions.left());
		actionList.addAll(actions.right().stream()
				.filter(a -> !a.type().equals(Action.Type.UNCHANGED) && !a.type().equals(Action.Type.UPDATED)).toList());

		// sort the actions by position
		actionList.sort((a, b) -> {
			int aIndex = a.left() != null ? a.left().index() : a.right() != null ? a.right().index() : 0;
			int bIndex = b.left() != null ? b.left().index() : b.right() != null ? b.right().index() : 0;
			return aIndex - bIndex;
		});

		// create the JSON string
		for (Action a : actionList) {
			if (sb.length() > 3)
				sb.append(",");
			sb.append("\n\t").append("{").append("\"type\":\"").append(a.type()).append("\",");

			if (a.left() != null) {
				sb.append("\"left\":").append(a.left().index()).append(",");
			}

			if (a.right() != null) {
				sb.append("\"right\":").append(a.right().index());
			}

			sb.append("}");
		}
		sb.append("]");

		// output the JSON
		System.out.println(sb.toString());
	}
}
