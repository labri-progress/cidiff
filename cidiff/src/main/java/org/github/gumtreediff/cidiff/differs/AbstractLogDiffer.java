package org.github.gumtreediff.cidiff.differs;

import org.github.gumtreediff.cidiff.LogDiffer;

import java.util.Properties;

public abstract class AbstractLogDiffer implements LogDiffer {
    protected final Properties options;

    public AbstractLogDiffer(Properties options) {
        this.options = options;
    }
}
