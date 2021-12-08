package org.github.gumtreediff.cidiff;

import java.util.Properties;

public abstract class AbstractStepDiffer implements StepDiffer {
    protected final Properties options;

    public AbstractStepDiffer(Properties options) {
        this.options = options;
    }
}
