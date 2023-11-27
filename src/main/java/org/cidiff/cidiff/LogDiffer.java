package org.cidiff.cidiff;


import org.cidiff.cidiff.differs.BruteForceLogDiffer;
import org.cidiff.cidiff.differs.HashDiffer;
import org.cidiff.cidiff.differs.LcsLogDiffer;
import org.cidiff.cidiff.differs.SeedDiffer;

import java.util.List;
import java.util.function.Supplier;

@FunctionalInterface
public interface LogDiffer {

	Pair<List<Action>> diff(List<Line> leftLines, List<Line> rightLines);

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
