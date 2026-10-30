/*
 * @test
 * @summary JLS 18.5.2: invocation type inference determines type arguments for a
 *          generic method from the target type and/or the argument types.
 * @run main GenericMethodInference
 */

import java.util.Collections;
import java.util.List;

public class GenericMethodInference {
    static <T> T pick(T a, T b) { return a; }
    static <T> List<T> singleton(T a) { return Collections.singletonList(a); }

    public static void main(String[] args) {
        String s = pick("a", "b");          // target-typed inference
        if (!"a".equals(s)) throw new AssertionError("pick String");

        Integer i = pick(1, 2);             // inference from argument types
        if (i != 1) throw new AssertionError("pick Integer");

        List<String> l = singleton("x");    // inference from target type
        if (!"x".equals(l.get(0))) throw new AssertionError("singleton");

        System.out.println("PASS");
    }
}
