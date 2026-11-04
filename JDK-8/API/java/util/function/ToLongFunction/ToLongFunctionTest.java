/*
 * @test
 * @summary java.util.function.ToLongFunction API conformance: applyAsLong.
 * @run main ToLongFunctionTest
 */

import java.util.function.ToLongFunction;

public class ToLongFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testToLongFunction() {
        ToLongFunction<String> f = s -> s.length();
        check(f.applyAsLong("abcd") == 4L, "applyAsLong");
    }

    public static void main(String[] args) {
        testToLongFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
