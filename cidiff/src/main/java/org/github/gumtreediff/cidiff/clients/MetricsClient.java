package org.github.gumtreediff.cidiff.clients;

import java.util.Properties;

import org.github.gumtreediff.cidiff.Action;

public class MetricsClient extends AbstractDiffClient {
    public MetricsClient(String leftFile, String rightFile, Properties options) {
        super(leftFile, rightFile, options);
    }

    @Override
    public void execute() {
        System.out.println("Left log: " + lines.left.size() + " lines.");
        System.out.println("Right log: " + lines.right.size() + " lines.");
        final Metrics metrics = getMetrics();
        System.out.println("Added lines: " + metrics.added + " lines.");
        System.out.println("Deleted lines: " + metrics.deleted + " lines.");
        System.out.println("Unchanged lines: " + metrics.unchanged + " lines.");
        System.out.println("Updated lines: " + metrics.updated + " lines.");
    }

    private Metrics getMetrics() {
        final Metrics metrics = new Metrics();
        for (Action a: actions.left.values())
            if (a.type == Action.Type.DELETED)
                metrics.deleted++;
            else if (a.type == Action.Type.UNCHANGED)
                metrics.unchanged++;
            else if (a.type == Action.Type.UPDATED)
                metrics.updated++;

        for (Action a: actions.right.values())
            if (a.type == Action.Type.ADDED)
                metrics.added++;

        return metrics;
    }

    private static class Metrics {
        int added = 0;
        int updated = 0;
        int unchanged = 0;
        int deleted = 0;
    }
}
