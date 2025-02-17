package org.github.cidiff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {

	@Test
	void logsim() {
		assertEquals(0, Metric.logsim("A B C", "A B C D", 0.6));  // nb token different
		assertEquals(1, Metric.logsim("A B C", "A B C", 0.6));  // identical
		assertEquals(2.5/3, Metric.logsim("A B C", "A B D", 0.6));  // one token different but same size
		assertEquals(2.0/3, Metric.logsim("A B C", "A B DD", 0.6));  // one token different with different size
		assertEquals(2.0/3, Metric.logsim("A B C", "A D E", 0.6));  // two tokens different but same size
		assertEquals(1.0/3, Metric.logsim("A B C", "A DD EE", 0.6));  // two tokens different with different size
		assertEquals(2.5/3, Metric.logsim("A BBBB C", "A BBBC C", 0.6));  // one token different but qgram identical
		assertEquals(2.0/3, Metric.logsim("A BBBB C", "A BBBC D", 0.6));  // one token different but qgram identical and one token different but same size
		assertEquals(0.5, Metric.logsim("A BBBB C", "A BBBC DD", 0.6));  // one token different but qgram identical and one token different with different size
	}

}