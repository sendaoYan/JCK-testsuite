/*
 * @test
 * @summary JLS 18.5.2: a target type flows through a chain of generic method
 *          invocations, so the type argument is inferred at the innermost call
 *          from the outermost context.
 * @run main TargetTypeChainedInference
 */

import java.util.Collections;
import java.util.List;

public class TargetTypeChainedInference {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static <T> List<T> singleton(T a) { return Collections.singletonList(a); }

    public static void main(String[] args) {
        // Target List<String> flows into singleton(...) so T is inferred as String.
        List<String> l = singleton("x");
        check("x".equals(l.get(0)), "target type infers T=String");

        // Nested: the outer generic call passes its result into the inner one.
        List<List<String>> outer = singleton(singleton("y"));
        check("y".equals(outer.get(0).get(0)), "nested chained inference");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
