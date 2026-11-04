/*
 * @test
 * @summary JLS 18.5.1 (Java SE 8): a generic method with a variable-arity
 *          parameter infers its type argument from the varargs elements.
 * @run main VarargsGenericMethod
 */

import java.util.ArrayList;
import java.util.List;

public class VarargsGenericMethod {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @SafeVarargs
    static <T> List<T> asList(T... items) {
        List<T> l = new ArrayList<>();
        for (T t : items) l.add(t);
        return l;
    }

    public static void main(String[] args) {
        List<String> s = asList("a", "b", "c");
        check(s.size() == 3 && "a".equals(s.get(0)), "varargs infers T=String");

        List<Integer> n = asList(1, 2, 3);
        check(n.size() == 3 && n.get(0) == 1, "varargs infers T=Integer");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
