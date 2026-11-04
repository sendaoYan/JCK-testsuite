/*
 * @test
 * @summary java.util.function.BiFunction API conformance: apply and andThen.
 * @run main BiFunctionTest
 */

import java.util.function.BiFunction;
import java.util.function.Function;

public class BiFunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBiFunction() {
        BiFunction<String, String, Integer> lenSum = (a, b) -> a.length() + b.length();
        check(lenSum.apply("ab", "cde") == 5, "apply");
        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
        Function<Integer, Integer> neg = x -> -x;
        check(add.andThen(neg).apply(3, 4) == -7, "andThen");
    }

    public static void main(String[] args) {
        testBiFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
