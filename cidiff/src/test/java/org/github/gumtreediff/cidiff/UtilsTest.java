package org.github.gumtreediff.cidiff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {
    @Test
    void rewriteSimTest() {
        assertEquals(0.0, Utils.rewriteSim("A B C", "A B C D"), 0.001);
        assertEquals(0.666, Utils.rewriteSim("A B C", "A EE C"), 0.001);
        assertEquals(0.333, Utils.rewriteSim("A B C", "A EE FF"), 0.001);
        assertEquals(0.0, Utils.rewriteSim("A B C", "GG EE FF"), 0.001);
    }

    @Test
    void testFastExponentiation() {
        assertEquals(1, Utils.fastExponentiation(2, 0));
        assertEquals(2, Utils.fastExponentiation(2, 1));
        assertEquals(4, Utils.fastExponentiation(2, 2));
        assertEquals(8, Utils.fastExponentiation(2, 3));
        assertEquals(16, Utils.fastExponentiation(2, 4));
    }
}
