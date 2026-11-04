/*
 * @test
 * @summary JLS 15.13 (Java SE 8): a method reference is target-typed to a
 *          functional interface; it may reference a static method, an instance
 *          method of a particular object, an instance method of an arbitrary
 *          object of a type, or a constructor.
 * @run main MethodReferenceTargetTyping
 */

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class MethodReferenceTargetTyping {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static String upper(String s) { return s.toUpperCase(); }

    public static void main(String[] args) {
        Function<String, Integer> len = String::length;
        check(len.apply("abc") == 3, "unbound instance method reference");
        UnaryOperator<String> toUp = String::toUpperCase;
        check(toUp.apply("x").equals("X"), "unbound instance method with receiver");

        Function<String, String> up = MethodReferenceTargetTyping::upper;
        check(up.apply("hi").equals("HI"), "static method reference");

        String s = "hello";
        Supplier<Integer> sLen = s::length;
        check(sLen.get() == 5, "bound instance method reference");

        Supplier<StringBuilder> ctor = StringBuilder::new;
        check(ctor.get().capacity() >= 0, "no-arg constructor reference");
        IntFunction<StringBuilder> ctorCap = StringBuilder::new;
        check(ctorCap.apply(16).capacity() == 16, "constructor reference with argument");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
