/*
 * @test
 * @summary JLS 15.27/15.13 (Java SE 8): lambda expressions and method reference
 *          expressions implement functional interfaces.
 * @run main LambdaAndMethodRefTest
 */
import java.util.function.*;

public class LambdaAndMethodRefTest {
    public static void main(String[] args) {
        Function<Integer, Integer> square = x -> x * x;
        if (square.apply(5) != 25) throw new AssertionError("lambda");

        Function<String, Integer> len = String::length;   // instance method reference
        if (len.apply("abcd") != 4) throw new AssertionError("instance method reference");

        Supplier<Integer> sup = () -> 42;
        if (sup.get() != 42) throw new AssertionError("supplier lambda");

        BiFunction<Integer, Integer, Integer> max = Math::max;   // static method reference
        if (max.apply(3, 9) != 9) throw new AssertionError("static method reference");

        System.out.println("PASS");
    }
}
