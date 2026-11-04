/*
 * @test
 * @summary JLS 18.5.3 (Java SE 8): a lambda is a poly expression; its parameter
 *          and return types are inferred from the target functional interface
 *          type, including when passed to a generic method.
 * @run main PolyExpressionLambda
 */

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PolyExpressionLambda {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static <T, R> R apply(Function<T, R> f, T arg) { return f.apply(arg); }

    public static void main(String[] args) {
        Integer len = apply(s -> s.length(), "abc");
        check(len == 3, "lambda typed by generic method parameter");

        List<String> l = new ArrayList<>();
        l.replaceAll(s -> s.toUpperCase());
        check(l.isEmpty(), "no-op replaceAll");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
