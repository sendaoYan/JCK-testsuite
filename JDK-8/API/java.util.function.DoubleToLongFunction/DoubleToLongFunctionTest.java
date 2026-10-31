/*
 * @test
 * @summary java.util.function.DoubleToLongFunction API conformance: applyAsLong.
 * @run main DoubleToLongFunctionTest
 */

import java.util.function.DoubleToLongFunction;

public class DoubleToLongFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDoubleToLongFunction() {
        DoubleToLongFunction trunc = d -> (long) d;
        check(trunc.applyAsLong(3.9) == 3L, "applyAsLong");
    }

    public static void main(String[] args) {
        testDoubleToLongFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
