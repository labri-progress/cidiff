package org.github.gumtreediff.cidiff.differs;

import java.util.Properties;

import org.github.gumtreediff.cidiff.LogDiffer;

public abstract class AbstractLogDiffer implements LogDiffer {
    protected final Properties options;

    public AbstractLogDiffer(Properties options) {
        this.options = options;
    }
}
