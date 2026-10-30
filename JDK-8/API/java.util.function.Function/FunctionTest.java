/*
 * @test
 * @summary java.util.function.Function API conformance: apply, andThen, compose, and
 *          static identity.
 * @run main FunctionTest
 */

import java.util.function.Function;

public class FunctionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFunction() {
        Function<String, Integer> len = String::length;
        check(len.apply("abc") == 3, "apply");
        Function<Integer, Integer> doubleIt = x -> x * 2;
        check(len.andThen(doubleIt).apply("abc") == 6, "andThen");
        Function<Integer, String> str = Object::toString;
        check(len.compose(str).apply(123) == 3, "compose");
        check("x".equals(Function.identity().apply("x")), "identity");
    }

    public static void main(String[] args) {
        testFunction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
