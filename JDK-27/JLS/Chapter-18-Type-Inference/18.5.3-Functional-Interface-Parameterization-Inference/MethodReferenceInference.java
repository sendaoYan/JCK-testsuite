/*
 * @test
 * @summary JLS 18.5.3: a method reference's type is inferred from the target
 *          functional interface type.
 * @run main MethodReferenceInference
 */

import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;

public class MethodReferenceInference {
    public static void main(String[] args) {
        Function<String, Integer> f = String::length;   // unbound instance reference
        if (f.apply("abcd") != 4) throw new AssertionError("String::length");

        IntBinaryOperator m = Math::max;                // static reference
        if (m.applyAsInt(3, 5) != 5) throw new AssertionError("Math::max");

        Supplier<String> s = () -> "hi";                // lambda, no parameters
        if (!"hi".equals(s.get())) throw new AssertionError("Supplier");

        System.out.println("PASS");
    }
}
