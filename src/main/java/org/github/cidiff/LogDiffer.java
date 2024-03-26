package org.github.cidiff;


import org.github.cidiff.differs.BruteForceLogDiffer;
import org.github.cidiff.differs.HashDiffer;
import org.github.cidiff.differs.LcsLogDiffer;
import org.github.cidiff.differs.SeedDiffer;

import java.util.List;
import java.util.function.Supplier;

@FunctionalInterface
public interface LogDiffer {

	Pair<List<Action>> diff(List<Line> leftLines, List<Line> rightLines, Options options);

	enum Algorithm {
		BRUTE_FORCE(BruteForceLogDiffer::new),
		LCS(LcsLogDiffer::new),
		SEED(SeedDiffer::new),
		HASH(HashDiffer::new);

		private final Supplier<LogDiffer> constructor;

		Algorithm(Supplier<LogDiffer> constructor) {
			this.constructor = constructor;
		}

		public LogDiffer construct() {
			return this.constructor.get();
		}
	}

}
