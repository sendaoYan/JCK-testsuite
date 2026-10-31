/*
 * @test
 * @summary java.util.function.IntToDoubleFunction API conformance: applyAsDouble.
 * @run main IntToDoubleFunctionTest
 */

import java.util.function.IntToDoubleFunction;

public class IntToDoubleFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIntToDoubleFunction() {
        IntToDoubleFunction half = i -> i * 1.5;
        check(half.applyAsDouble(2) == 3.0, "applyAsDouble");
    }

    public static void main(String[] args) {
        testIntToDoubleFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
