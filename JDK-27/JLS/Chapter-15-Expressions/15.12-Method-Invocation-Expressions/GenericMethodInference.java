/*
 * @test
 * @summary JLS 15.12.2.8 (with 18): the type arguments of a generic method
 *          invocation are inferred from the argument types and the invocation
 *          target type; explicit type arguments override inference.
 * @run main GenericMethodInference
 */

import java.util.Collections;
import java.util.List;

public class GenericMethodInference {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static <T> T identity(T x) { return x; }
    static <T> T pick(T a, T b) { return a; }

    public static void main(String[] args) {
        check(identity("s").equals("s"), "T inferred as String");
        check(identity(1) == 1, "T inferred as Integer (boxed)");

        // target type drives inference
        String s = identity("a");
        check(s.equals("a"), "target-typed identity");
        List<String> l = Collections.emptyList();
        check(l.isEmpty(), "emptyList target type String");

        // explicit type argument
        check(Collections.<Integer>emptyList().isEmpty(), "explicit type argument");

        // both arguments constrain T to a common type
        String s2 = pick("a", "b");
        check(s2.equals("a"), "pick T=String");
        Integer i2 = pick(1, 2);
        check(i2 == 1, "pick T=Integer (boxed)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
