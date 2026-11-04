/*
 * @test
 * @summary java.util.function.LongBinaryOperator API conformance: applyAsLong.
 * @run main LongBinaryOperatorTest
 */

import java.util.function.LongBinaryOperator;

public class LongBinaryOperatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLongBinaryOperator() {
        LongBinaryOperator mul = (a, b) -> a * b;
        check(mul.applyAsLong(6L, 7L) == 42L, "applyAsLong");
    }

    public static void main(String[] args) {
        testLongBinaryOperator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
