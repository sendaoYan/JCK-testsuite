/*
 * @test
 * @summary java.util.function.DoubleFunction API conformance: apply.
 * @run main DoubleFunctionTest
 */

import java.util.function.DoubleFunction;

public class DoubleFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDoubleFunction() {
        DoubleFunction<String> f = d -> "v=" + d;
        check("v=2.5".equals(f.apply(2.5)), "apply");
    }

    public static void main(String[] args) {
        testDoubleFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
