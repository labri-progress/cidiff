package org.github.cidiff;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

class LCSTest {

	void testLCS(BiFunction<List<String>, List<String>, List<Pair<String>>> lcsFunction) {
		List<String> left = List.of("foo", "bar", "foo");
		List<String> right = List.of("foo", "bar");
		List<Pair<String>> lcs = lcsFunction.apply(left, right);
		assertEquals(lcs.size(), 2);
		assertEquals(lcs.get(0).left(), left.get(0));
		assertEquals(lcs.get(0).right(), right.get(0));
		assertEquals(lcs.get(1).left(), left.get(1));
		assertEquals(lcs.get(1).right(), right.get(1));

		left = List.of("foo", "foo", "foo");
		right = List.of("foo", "foo");
		lcs = lcsFunction.apply(left, right);
		assertEquals(lcs.size(), 2);
		assertEquals(lcs.get(0).left(), left.get(1));
		assertEquals(lcs.get(0).right(), right.get(0));
		assertEquals(lcs.get(1).left(), left.get(2));
		assertEquals(lcs.get(1).right(), right.get(1));

		left = List.of("foo", "bar", "foo");
		right = List.of("foo", "foo");
		lcs = lcsFunction.apply(left, right);
		assertEquals(lcs.size(), 2);
		assertEquals(lcs.get(0).left(), left.get(0));
		assertEquals(lcs.get(0).right(), right.get(0));
		assertEquals(lcs.get(1).left(), left.get(2));
		assertEquals(lcs.get(1).right(), right.get(1));

		left = List.of("foo", "foo");
		right = List.of("foo", "bar", "foo");
		lcs = lcsFunction.apply(left, right);
		assertEquals(lcs.size(), 2);
		assertEquals(lcs.get(0).left(), left.get(0));
		assertEquals(lcs.get(0).right(), right.get(0));
		assertEquals(lcs.get(1).left(), left.get(1));
		assertEquals(lcs.get(1).right(), right.get(2));

		left = List.of();
		right = List.of("foo", "bar");
		lcs = lcsFunction.apply(left, right);
		assertEquals(lcs.size(), 0);

		left = List.of("foo", "bar");
		right = List.of();
		lcs = lcsFunction.apply(left, right);
		assertEquals(lcs.size(), 0);
	}

	@Test
	void hirschberg() {
		testLCS((left, right) -> LCS.hirschberg(left, right, String::equals));
	}

	@Test
	void myers() {
		testLCS((left, right) -> LCS.myers(left, right, String::equals));
	}

}