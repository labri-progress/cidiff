package org.github.gumtreediff.cidiff;

import java.util.Properties;

public abstract class AbstractLogDiffer implements LogDiffer {
    protected final Properties options;

    public AbstractLogDiffer(Properties options) {
        this.options = options;
    }
}
