package org.github.gumtreediff.cidiff;

import java.util.List;
import java.util.Properties;

public interface StepDiffer {
    enum Algorithm {
        BRUTE_FORCE,
        LCS
    }
    Pair<Action[]> diffStep(Pair<List<String>> lines);

    static StepDiffer get(Algorithm algorithm, Properties options) {
        return switch (algorithm) {
            case BRUTE_FORCE -> new BruteForceStepDiffer(options);
            case LCS -> new LcsStepDiffer(options);
        };
    }
}
