/*
 * @test
 * @summary java.lang.reflect.TypeVariable conformance: name/bounds/generic declaration.
 * @run main TypeVariableTest
 */

import java.lang.reflect.TypeVariable;

public class TypeVariableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample<T extends Number & Comparable<T>> {
    }

    static void testTypeVariable() {
        TypeVariable<?>[] tvs = Sample.class.getTypeParameters();
        check(tvs.length == 1, "one type variable");
        TypeVariable<?> tv = tvs[0];
        check("T".equals(tv.getName()), "getName");
        check(tv.getBounds().length == 2, "two bounds");
        check(tv.getBounds()[0] == Number.class, "first bound Number");
        check(tv.getGenericDeclaration() == Sample.class, "getGenericDeclaration");
        check(tv.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testTypeVariable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
