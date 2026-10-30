/*
 * @test
 * @summary JLS 18.5.3: functional interface parameterization inference derives a
 *          lambda's parameter types from the target functional interface type.
 * @run main LambdaTargetType
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class LambdaTargetType {
    public static void main(String[] args) {
        Function<String, Integer> f = s -> s.length();
        if (f.apply("abc") != 3) throw new AssertionError("Function lambda");

        Comparator<String> c = (a, b) -> a.compareTo(b);
        List<String> l = new ArrayList<>(Arrays.asList("b", "a"));
        l.sort(c);
        if (!"a".equals(l.get(0))) throw new AssertionError("Comparator lambda");

        System.out.println("PASS");
    }
}
