package org.github.gumtreediff.cidiff;

import org.github.gumtreediff.cidiff.differs.AlternatingBruteForceLogDiffer;
import org.github.gumtreediff.cidiff.differs.BruteForceLogDiffer;
import org.github.gumtreediff.cidiff.differs.LcsLogDiffer;
import org.github.gumtreediff.cidiff.differs.SeedExtendDiffer;

import java.util.List;
import java.util.Properties;

public interface LogDiffer {
    enum Algorithm {
        ALTERNATING_BRUTE_FORCE,
        BRUTE_FORCE,
        LCS,
        SEED_EXTEND,
    }

    Pair<Action[]> diff(Pair<List<String>> lines);

    static LogDiffer get(Algorithm algorithm, Properties options) {
        return switch (algorithm) {
            case BRUTE_FORCE -> new AlternatingBruteForceLogDiffer(options);
            case ALTERNATING_BRUTE_FORCE -> new BruteForceLogDiffer(options);
            case LCS -> new LcsLogDiffer(options);
            case SEED_EXTEND -> new SeedExtendDiffer(options);
        };
    }
}
