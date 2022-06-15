package org.github.gumtreediff.cidiff.parsers;

import java.util.Properties;

import org.github.gumtreediff.cidiff.LogParser;

public abstract class AbstractLogParser implements LogParser {
    protected final Properties options;

    public AbstractLogParser(Properties options) {
        this.options = options;
    }
}
