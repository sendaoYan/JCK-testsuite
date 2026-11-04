/*
 * @test
 * @summary java.util.function.ToIntBiFunction API conformance: applyAsInt.
 * @run main ToIntBiFunctionTest
 */

import java.util.function.ToIntBiFunction;

public class ToIntBiFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testToIntBiFunction() {
        ToIntBiFunction<String, String> f = (a, b) -> a.length() + b.length();
        check(f.applyAsInt("ab", "cde") == 5, "applyAsInt");
    }

    public static void main(String[] args) {
        testToIntBiFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
