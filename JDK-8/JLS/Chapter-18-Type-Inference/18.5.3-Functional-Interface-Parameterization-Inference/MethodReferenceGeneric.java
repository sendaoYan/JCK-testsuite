/*
 * @test
 * @summary JLS 18.5.3 (Java SE 8): a method reference to a generic method has its
 *          type arguments inferred from the target functional interface type.
 * @run main MethodReferenceGeneric
 */

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class MethodReferenceGeneric {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static <T> List<T> singleton(T a) { return Arrays.asList(a); }

    public static void main(String[] args) {
        Function<String, List<String>> f = MethodReferenceGeneric::singleton;
        check("a".equals(f.apply("a").get(0)), "generic method reference infers T=String");

        Supplier<List<Integer>> s = () -> singleton(1);
        check(s.get().get(0) == 1, "generic method reference via lambda target");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
