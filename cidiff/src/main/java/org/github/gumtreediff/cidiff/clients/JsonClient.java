package org.github.gumtreediff.cidiff.clients;

import org.github.gumtreediff.cidiff.Action;
import org.github.gumtreediff.cidiff.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class JsonClient extends AbstractDiffClient {

    public JsonClient(String leftFile, String rightFile, Properties options) {
        super(leftFile, rightFile, options);
    }

    public void execute() {
        final var leftLines = lines.left;
        final var rightLines = lines.right;
        final var actions = differ.diff(new Pair<>(leftLines, rightLines));

        final var sb = new StringBuffer("[");

        // deduplicate the list of actions
        final var actionList = new ArrayList<>(Arrays.asList(actions.left));
        actionList.addAll(Arrays.asList(actions.right).stream()
                .filter(a -> !a.type.equals(Action.Type.UNCHANGED) && !a.type.equals(Action.Type.UPDATED)).toList());

        // sort the actions by position
        actionList.sort((a, b) -> {
            if (a.leftLocation == -1 && b.leftLocation == -1) {
                return 0;
            } else if (a.leftLocation == -1 || b.leftLocation == -1) {
                return a.rightLocation - b.rightLocation;
            } else {
                return a.leftLocation - b.leftLocation;
            }
        });

        // create the JSON string
        for (Action a : actionList) {
            if (sb.length() > 3)
                sb.append(",");
            sb.append("\n\t").append("{")
                    .append("\"type\":\"").append(a.type).append("\",")
                    .append("\"left\":").append(a.leftLocation).append(",")
                    .append("\"right\":").append(a.rightLocation)
                    .append("}");
        }
        sb.append("]");

        // output the JSON
        System.out.println(sb.toString());
    }
}
