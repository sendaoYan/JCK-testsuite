/*
 * @test
 * @summary JLS 15.27 (Java SE 8): a lambda expression is target-typed to a
 *          functional interface; it may use an expression or block body,
 *          declared or inferred parameter types, and may capture effectively
 *          final locals.
 * @run main LambdaTargetTyping
 */

import java.util.concurrent.Callable;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class LambdaTargetTyping {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Function<String, Integer> f = s -> s.length();
        check(f.apply("abc") == 3, "expression-body lambda");

        IntBinaryOperator add = (a, b) -> a + b;
        check(add.applyAsInt(2, 3) == 5, "two inferred parameters");

        Function<String, String> g = s -> { return s.toUpperCase(); };
        check(g.apply("x").equals("X"), "block-body lambda");

        Supplier<String> sup = () -> "hello";
        check(sup.get().equals("hello"), "no parameters");

        UnaryOperator<Integer> inc = x -> x + 1;
        check(inc.apply(1) == 2, "single inferred parameter without parentheses");

        BinaryOperator<Integer> sum = (Integer a, Integer b) -> a + b;
        check(sum.apply(1, 2) == 3, "declared parameter types");

        int captured = 10;
        IntSupplier read = () -> captured;
        check(read.getAsInt() == 10, "effectively final capture");

        Callable<String> call = () -> "done";
        try {
            check(call.call().equals("done"), "lambda implementing a checked-exception interface");
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
