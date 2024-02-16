package org.cidiff.cidiff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

	@Test
	void logsim() {
		assertEquals(0, Metric.logsim("A B C", "A B C D"));  // nb token different
		assertEquals(1, Metric.logsim("A B C", "A B C"));  // identical
		assertEquals(2.5/3, Metric.logsim("A B C", "A B D"));  // one token different but same size
		assertEquals(2.0/3, Metric.logsim("A B C", "A B DD"));  // one token different with different size
		assertEquals(2.0/3, Metric.logsim("A B C", "A D E"));  // two tokens different but same size
		assertEquals(1.0/3, Metric.logsim("A B C", "A DD EE"));  // two tokens different with different size
		assertEquals(2.5/3, Metric.logsim("A BBBB C", "A BBBC C"));  // one token different but lcs identical
		assertEquals(2.0/3, Metric.logsim("A BBBB C", "A BBBC D"));  // one token different but lcs identical and one token different but same size
		assertEquals(0.5, Metric.logsim("A BBBB C", "A BBBC DD"));  // one token different but lcs identical and one token different with different size
	}

	@Test
	void lcsLength() {
		assertEquals(2, Utils.lcsLength("abcd".toCharArray(), "abef".toCharArray()));
		assertEquals(2, Utils.lcsLength("abcd".toCharArray(), "faeb".toCharArray()));
		assertEquals(2, Utils.lcsLength("abcd".toCharArray(), "gchhdi".toCharArray()));
		assertEquals(1, Utils.lcsLength("abcd".toCharArray(), "jruijfb".toCharArray()));
		assertEquals(3, Utils.lcsLength("abcd".toCharArray(), "auhbefdoko".toCharArray()));
		assertEquals(0, Utils.lcsLength("abcd".toCharArray(), "efghi".toCharArray()));
	}
}