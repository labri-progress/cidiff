package org.github.gumtreediff.cidiff.clients;

import java.util.List;
import java.util.Properties;

import org.github.gumtreediff.cidiff.*;

public abstract class AbstractDiffClient implements DiffClient {
    protected static final String DEFAULT_DIFFER = "BRUTE_FORCE";

    public final Properties options;
    public final LogDiffer differ;
    public final Pair<String> files;
    public final Pair<List<String>> lines;
    public final Pair<Action[]> actions;

    public AbstractDiffClient(String leftFile, String rightFile, Properties options) {
        this.options = options;
        this.files = new Pair<>(leftFile, rightFile);
        this.differ = LogDiffer.get(LogDiffer.Algorithm.valueOf(
                options.getProperty(Options.DIFFER, DEFAULT_DIFFER)), options);
        this.lines = LogParser.parseLogs(this.files, options);
        this.actions = this.differ.diff(this.lines);
    }
}
