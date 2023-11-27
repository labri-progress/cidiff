package org.cidiff.cidiff.clients;

import org.cidiff.cidiff.Action;
import org.cidiff.cidiff.Line;
import org.cidiff.cidiff.Pair;

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
				.filter(a -> !a.type.equals(Action.Type.UNCHANGED) && !a.type.equals(Action.Type.UPDATED)).toList());

		// sort the actions by position
		actionList.sort((a, b) -> {
			int aIndex = a.leftLogLine != null ? a.leftLogLine.index() : a.rightLogLine != null ? a.rightLogLine.index() : 0;
			int bIndex = b.leftLogLine != null ? b.leftLogLine.index() : b.rightLogLine != null ? b.rightLogLine.index() : 0;
			return aIndex - bIndex;
		});

		// create the JSON string
		for (Action a : actionList) {
			if (sb.length() > 3)
				sb.append(",");
			sb.append("\n\t").append("{").append("\"type\":\"").append(a.type).append("\",");

			if (a.leftLogLine != null) {
				sb.append("\"left\":").append(a.leftLogLine.index()).append(",");
			}

			if (a.rightLogLine != null) {
				sb.append("\"right\":").append(a.rightLogLine.index());
			}

			sb.append("}");
		}
		sb.append("]");

		// output the JSON
		System.out.println(sb.toString());
	}
}
