/*
 * @test
 * @summary java.util.function.LongToDoubleFunction API conformance: applyAsDouble.
 * @run main LongToDoubleFunctionTest
 */

import java.util.function.LongToDoubleFunction;

public class LongToDoubleFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLongToDoubleFunction() {
        LongToDoubleFunction half = l -> l * 1.5;
        check(half.applyAsDouble(2L) == 3.0, "applyAsDouble");
    }

    public static void main(String[] args) {
        testLongToDoubleFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
