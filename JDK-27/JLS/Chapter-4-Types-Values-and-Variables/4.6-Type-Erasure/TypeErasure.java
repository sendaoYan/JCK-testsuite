/*
 * @test
 * @summary JLS 4.6: type erasure maps parameterized types to their raw type,
 *          arrays to the erasure of the component, and a type variable to the
 *          erasure of its leftmost bound. The erasure of a generic method's
 *          signature has no type parameters.
 * @run main TypeErasure
 */

import java.util.List;
import java.util.ArrayList;

class LeftmostBound<T extends Number> {
    void m(T t) {}
}

public class TypeErasure {
    // The erasure of <U extends Number> U f() is Number f().
    public static <U extends Number> U f() { return null; }

    public static void main(String[] args) throws Exception {
        // Erasure of a parameterized type G<T1,...,Tn> is |G|.
        List<String> ls = new ArrayList<String>();
        if (ls.getClass() != ArrayList.class) throw new AssertionError("erasure of List<String>");

        // Erasure of an array type T[] is |T|[].
        Object o = new String[0];
        if (!(o instanceof String[])) throw new AssertionError("erasure of String[]");

        // Erasure of a type variable is the erasure of its leftmost bound.
        // <T extends Number> T erases to Number; verify via reflection.
        var m = TypeErasure.class.getMethod("f");
        if (m.getReturnType() != Number.class) {
            throw new AssertionError("erasure of <U extends Number> f() must return Number");
        }

        // The erased parameter type of <T extends Number> void m(T) is Number.
        var lm = LeftmostBound.class.getDeclaredMethod("m", Number.class);
        if (lm.getParameterTypes()[0] != Number.class) {
            throw new AssertionError("erasure of parameter T extends Number must be Number");
        }

        System.out.println("PASS");
    }
}
