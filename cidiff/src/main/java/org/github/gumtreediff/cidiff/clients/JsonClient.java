package org.github.gumtreediff.cidiff.clients;

import java.util.ArrayList;
import java.util.Properties;

import org.github.gumtreediff.cidiff.Action;
import org.github.gumtreediff.cidiff.Pair;

public final class JsonClient extends AbstractDiffClient {

    public JsonClient(String leftFile, String rightFile, Properties options) {
        super(leftFile, rightFile, options);
    }

    public void execute() {
        final var leftLines = lines.left;
        final var rightLines = lines.right;
        final var actions = differ.diff(new Pair<>(leftLines, rightLines));

        final var sb = new StringBuilder("[");

        // deduplicate the list of actions
        final var actionList = new ArrayList<>(actions.left.values());
        actionList.addAll(actions.right.values().stream()
                .filter(a -> !a.type.equals(Action.Type.UNCHANGED) && !a.type.equals(Action.Type.UPDATED)).toList());

        // sort the actions by position
        actionList.sort((a, b) -> {
            if (a.leftLogLine == null && b.leftLogLine == null) {
                return 0;
            }
            else if (a.leftLogLine == null || b.leftLogLine == null) {
                return a.rightLogLine.lineNumber - b.rightLogLine.lineNumber;
            }
            else {
                return a.leftLogLine.lineNumber - b.leftLogLine.lineNumber;
            }
        });

        // create the JSON string
        for (Action a : actionList) {
            if (sb.length() > 3)
                sb.append(",");
            sb.append("\n\t").append("{")
                    .append("\"type\":\"").append(a.type).append("\",")
                    .append("\"left\":").append(a.leftLogLine.lineNumber).append(",")
                    .append("\"right\":").append(a.rightLogLine.lineNumber)
                    .append("}");
        }
        sb.append("]");

        // output the JSON
        System.out.println(sb.toString());
    }
}
