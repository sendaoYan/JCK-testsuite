/*
 * @test
 * @summary java.util.function.LongToIntFunction API conformance: applyAsInt.
 * @run main LongToIntFunctionTest
 */

import java.util.function.LongToIntFunction;

public class LongToIntFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLongToIntFunction() {
        LongToIntFunction trunc = l -> (int) l;
        check(trunc.applyAsInt(200L) == 200, "applyAsInt");
    }

    public static void main(String[] args) {
        testLongToIntFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
