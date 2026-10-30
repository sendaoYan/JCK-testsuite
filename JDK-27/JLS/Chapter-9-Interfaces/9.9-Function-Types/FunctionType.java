/*
 * @test
 * @summary JLS 9.9: a function type is defined by a functional interface; a
 *          method reference and a lambda both conform to the same function type.
 * @run main FunctionType
 */

import java.util.function.Function;

public class FunctionType {
    public static void main(String[] args) {
        Function<String, Integer> viaLambda = s -> s.length();
        Function<String, Integer> viaMethodRef = String::length;
        if (viaLambda.apply("abc") != 3 || viaMethodRef.apply("abcd") != 4)
            throw new AssertionError("function type");
        System.out.println("PASS");
    }
}
