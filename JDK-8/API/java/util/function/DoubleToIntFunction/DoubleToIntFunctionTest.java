/*
 * @test
 * @summary java.util.function.DoubleToIntFunction API conformance: applyAsInt.
 * @run main DoubleToIntFunctionTest
 */

import java.util.function.DoubleToIntFunction;

public class DoubleToIntFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDoubleToIntFunction() {
        DoubleToIntFunction trunc = d -> (int) d;
        check(trunc.applyAsInt(3.9) == 3, "applyAsInt");
        check(trunc.applyAsInt(-3.9) == -3, "applyAsInt negative truncates toward zero");
    }

    public static void main(String[] args) {
        testDoubleToIntFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
