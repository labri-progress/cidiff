package org.github.gumtreediff.cidiff;

import java.util.*;

public interface LogFilter {

    enum Type {
        REWRITE,
        TERM_FREQUENCY
    }

    /**
     * Filters irrelevant lines from a log.
     * The irrelevant lines are removed in place.
     * @param log
     */
    void filter(List<LogLine> log);

    static LogFilter get(Type type, Properties options) {
        return switch (type) {
            case REWRITE -> new RewriteLogFilter(options);
            case TERM_FREQUENCY -> new TermFrequencyLogFilter(options);
        };
    }

    class RewriteLogFilter implements LogFilter {
        private static final String DEFAULT_REWRITE_MIN = "0.5";

        private final double rewriteMin;

        public RewriteLogFilter(Properties options) {
            rewriteMin = Double.parseDouble(
                    options.getProperty(Options.DIFFER_REWRITE_MIN, DEFAULT_REWRITE_MIN)
            );
        }

        @Override
        public void filter(List<LogLine> log) {
            LogLine previousLine = null;
            final Iterator<LogLine> logLineIterator = log.iterator();
            while (logLineIterator.hasNext()) {
                if (previousLine != null) {
                    final LogLine currentLine = logLineIterator.next();
                    if (Utils.rewriteSim(previousLine.value, currentLine.value) >= rewriteMin)
                        logLineIterator.remove();
                    else
                        previousLine = currentLine;
                }
                else
                    previousLine = logLineIterator.next();
            }
        }
    }

    class TermFrequencyLogFilter implements LogFilter {
        public static final int FREQUENCY_CUTOFF = 15;
        public static final double MIN_INFREQUENT = 2;

        public TermFrequencyLogFilter(Properties options) {
        }

        @Override
        public void filter(List<LogLine> log) {
            final Map<String, Integer> termFreqs = new HashMap<>();
            for (LogLine line : log)
                for (String term : Utils.split(line))
                    termFreqs.put(term, termFreqs.getOrDefault(term, 0) + 1);

            final Iterator<LogLine> logLineIterator = log.iterator();
            while (logLineIterator.hasNext()) {
                final LogLine line = logLineIterator.next();
                int frequentTerms = 0;
                final String[] terms = Utils.split(line);
                for (String term : terms)
                    if (termFreqs.get(term) >= FREQUENCY_CUTOFF)
                        frequentTerms++;

                if (terms.length - frequentTerms < MIN_INFREQUENT)
                    logLineIterator.remove();
            }
        }
    }
}
