/*
 * @test
 * @summary java.util.function.ToLongBiFunction API conformance: applyAsLong.
 * @run main ToLongBiFunctionTest
 */

import java.util.function.ToLongBiFunction;

public class ToLongBiFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testToLongBiFunction() {
        ToLongBiFunction<String, String> f = (a, b) -> (long) (a.length() + b.length());
        check(f.applyAsLong("ab", "cde") == 5L, "applyAsLong");
    }

    public static void main(String[] args) {
        testToLongBiFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
