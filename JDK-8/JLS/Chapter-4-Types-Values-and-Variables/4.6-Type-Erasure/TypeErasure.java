/*
 * @test
 * @summary JLS 4.6 (Java SE 8): type erasure maps a parameterized type to its
 *          raw type, an array type to the erasure of its component type, and a
 *          type variable to the erasure of its leftmost bound. The erasure of a
 *          generic method signature has no type parameters.
 * @run main TypeErasure
 */

import java.util.ArrayList;
import java.util.List;

class LeftmostBound<T extends Number> {
    void m(T t) {}
}

public class TypeErasure {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static <U extends Number> U f() { return null; }

    public static void main(String[] args) throws Exception {
        // Erasure of G<T1,...,Tn> is |G| (the raw type).
        List<String> ls = new ArrayList<String>();
        check(ls.getClass() == ArrayList.class, "erasure of List<String> is ArrayList");

        // Erasure of an array type T[] is |T|[].
        Object o = new String[0];
        check(o instanceof String[], "erasure of String[] is String[]");

        // Erasure of a type variable is the erasure of its leftmost bound.
        check(TypeErasure.class.getMethod("f").getReturnType() == Number.class,
              "erasure of <U extends Number> f() return type is Number");
        check(LeftmostBound.class.getDeclaredMethod("m", Number.class).getParameterTypes()[0]
              == Number.class, "erasure of parameter <T extends Number> is Number");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
