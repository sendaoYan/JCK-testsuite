/*
 * @test
 * @summary java.util.function.ToDoubleFunction API conformance: applyAsDouble.
 * @run main ToDoubleFunctionTest
 */

import java.util.function.ToDoubleFunction;

public class ToDoubleFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testToDoubleFunction() {
        ToDoubleFunction<String> f = s -> s.length();
        check(f.applyAsDouble("abcd") == 4.0, "applyAsDouble");
    }

    public static void main(String[] args) {
        testToDoubleFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
