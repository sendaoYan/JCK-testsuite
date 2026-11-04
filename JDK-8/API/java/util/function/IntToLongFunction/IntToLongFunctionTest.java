/*
 * @test
 * @summary java.util.function.IntToLongFunction API conformance: applyAsLong.
 * @run main IntToLongFunctionTest
 */

import java.util.function.IntToLongFunction;

public class IntToLongFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIntToLongFunction() {
        IntToLongFunction times100 = i -> i * 100L;
        check(times100.applyAsLong(2) == 200L, "applyAsLong");
    }

    public static void main(String[] args) {
        testIntToLongFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
