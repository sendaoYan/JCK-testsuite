/*
 * @test
 * @summary JLS 15.27: a lambda expression is target-typed, may have an
 *          expression or block body, and can capture effectively final local
 *          variables.
 * @run main LambdaExpressions
 */

import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

public class LambdaExpressions {
    public static void main(String[] args) {
        Function<String, Integer> f = s -> s.length();      // expression body
        if (f.apply("abc") != 3) throw new AssertionError("expression body");

        IntBinaryOperator add = (a, b) -> { int r = a + b; return r; };  // block body
        if (add.applyAsInt(2, 3) != 5) throw new AssertionError("block body");

        int base = 10;                                        // effectively final
        IntUnaryOperator g = x -> x + base;
        if (g.applyAsInt(5) != 15) throw new AssertionError("captured local");

        Supplier<String> s = () -> "hi";                     // zero parameters
        if (!"hi".equals(s.get())) throw new AssertionError("zero-arg lambda");

        System.out.println("PASS");
    }
}
