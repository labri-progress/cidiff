package org.github.gumtreediff.cidiff;

import java.util.List;

public interface StepDiffer {
    enum Algorithm {
        BRUTE_FORCE,
        LCS
    }
    Pair<Action[]> diffStep(Pair<List<String>> lines);

    static StepDiffer get(Algorithm algorithm) {
        switch (algorithm) {
            case BRUTE_FORCE:
                return new BruteForceStepDiffer();
            case LCS:
                return new LcsStepDiffer();
            default:
                throw new IllegalArgumentException("Unknown step differ " + algorithm);
        }
    }
}
