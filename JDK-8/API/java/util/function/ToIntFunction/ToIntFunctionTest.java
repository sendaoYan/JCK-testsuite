/*
 * @test
 * @summary java.util.function.ToIntFunction API conformance: applyAsInt.
 * @run main ToIntFunctionTest
 */

import java.util.function.ToIntFunction;

public class ToIntFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testToIntFunction() {
        ToIntFunction<String> f = s -> s.length();
        check(f.applyAsInt("abc") == 3, "applyAsInt");
        check(f.applyAsInt("") == 0, "applyAsInt empty");
    }

    public static void main(String[] args) {
        testToIntFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
