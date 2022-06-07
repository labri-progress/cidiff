package org.github.gumtreediff.cidiff.clients;

import org.github.gumtreediff.cidiff.Action;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class MetricsClient_modified extends AbstractDiffClient {
    public MetricsClient_modified(String leftFile, String rightFile, Properties options) {
        super(leftFile, rightFile, options);
    }

    @Override
    public void execute() {
        int lines[] = getNewLines();
        try {
            write(lines[0],lines[1]);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void write(int added_lines, int updated_lines) throws IOException {
        FileWriter w = new FileWriter("benchmark/precision_recall.csv",true);
        int total = added_lines+updated_lines;
        w.append(added_lines+";"+updated_lines+";"+total+"\n");
        w.close();
    }

    private int[] getNewLines() {
        int added_lines = 0;
        int updated_lines = 0;
        for (Action a: actions.left)
            if (a.type == Action.Type.UPDATED)
                updated_lines+=1;

        for (Action a: actions.right)
            if (a.type == Action.Type.ADDED)
                added_lines+=1;
        return new int[] {added_lines, updated_lines};
    }
}
