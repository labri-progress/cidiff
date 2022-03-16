package org.github.gumtreediff.cidiff.parsers;

import org.github.gumtreediff.cidiff.LogParser;

import java.util.Properties;

public abstract class AbstractLogParser implements LogParser {
    protected final Properties options;

    public AbstractLogParser(Properties options) {
        this.options = options;
    }
}
