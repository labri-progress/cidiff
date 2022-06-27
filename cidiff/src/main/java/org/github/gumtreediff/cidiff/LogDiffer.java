package org.github.gumtreediff.cidiff;

import java.util.List;
import java.util.Properties;

import org.github.gumtreediff.cidiff.differs.AlternatingBruteForceLogDiffer;
import org.github.gumtreediff.cidiff.differs.BruteForceLogDiffer;
import org.github.gumtreediff.cidiff.differs.LcsLogDiffer;
import org.github.gumtreediff.cidiff.differs.SeedExtendDiffer;

public interface LogDiffer {
    enum Algorithm {
        ALTERNATING_BRUTE_FORCE,
        BRUTE_FORCE,
        LCS,
        SEED_EXTEND,
    }

    Pair<Action[]> diff(Pair<List<LogLine>> lines);

    static LogDiffer get(Algorithm algorithm, Properties options) {
        return switch (algorithm) {
            case BRUTE_FORCE -> new BruteForceLogDiffer(options);
            case ALTERNATING_BRUTE_FORCE -> new AlternatingBruteForceLogDiffer(options);
            case LCS -> new LcsLogDiffer(options);
            case SEED_EXTEND -> new SeedExtendDiffer(options);
        };
    }
}
