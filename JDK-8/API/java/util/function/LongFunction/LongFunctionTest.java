/*
 * @test
 * @summary java.util.function.LongFunction API conformance: apply.
 * @run main LongFunctionTest
 */

import java.util.function.LongFunction;

public class LongFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLongFunction() {
        LongFunction<String> f = l -> "v=" + l;
        check("v=5".equals(f.apply(5L)), "apply");
    }

    public static void main(String[] args) {
        testLongFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
