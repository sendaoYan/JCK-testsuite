/*
 * @test
 * @summary java.util.function.IntFunction API conformance: apply.
 * @run main IntFunctionTest
 */

import java.util.function.IntFunction;

public class IntFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIntFunction() {
        IntFunction<String> f = x -> "n=" + x;
        check("n=5".equals(f.apply(5)), "apply");
    }

    public static void main(String[] args) {
        testIntFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
