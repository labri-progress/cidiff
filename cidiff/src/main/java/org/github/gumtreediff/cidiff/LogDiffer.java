package org.github.gumtreediff.cidiff;

import java.util.List;
import java.util.Properties;

import org.github.gumtreediff.cidiff.differs.*;

public interface LogDiffer {
    enum Algorithm {
        ALTERNATING_BRUTE_FORCE,
        BRUTE_FORCE,
        LCS,
        SEED_EXTEND,
        HASH,
    }

    Pair<Action[]> diff(Pair<List<LogLine>> lines);

    static LogDiffer get(Algorithm algorithm, Properties options) {
        return switch (algorithm) {
            case BRUTE_FORCE -> new BruteForceLogDiffer(options);
            case ALTERNATING_BRUTE_FORCE -> new AlternatingBruteForceLogDiffer(options);
            case LCS -> new LcsLogDiffer(options);
            case SEED_EXTEND -> new SeedExtendDiffer(options);
            case HASH -> new HashDiffer(options);
        };
    }
}
