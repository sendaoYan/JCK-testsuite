/*
 * @test
 * @summary JLS 15.13: a method reference may refer to a static method, an
 *          instance method of a particular object, an instance method of an
 *          arbitrary object of a type, or a constructor.
 * @run main MethodReference
 */

import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

public class MethodReference {
    static int twice(int x) { return x * 2; }

    public static void main(String[] args) {
        IntUnaryOperator f = MethodReference::twice;   // static
        if (f.applyAsInt(3) != 6) throw new AssertionError("static method reference");

        String s = "abc";
        Supplier<Integer> g = s::length;               // bound instance method
        if (g.get() != 3) throw new AssertionError("bound instance method reference");

        Function<String, Integer> h = String::length;  // unbound instance method
        if (h.apply("abcd") != 4) throw new AssertionError("unbound instance method reference");

        Supplier<StringBuilder> c = StringBuilder::new;  // constructor
        if (!(c.get() instanceof StringBuilder)) throw new AssertionError("constructor reference");

        System.out.println("PASS");
    }
}
