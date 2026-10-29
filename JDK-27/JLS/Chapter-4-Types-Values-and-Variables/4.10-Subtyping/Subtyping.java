/*
 * @test
 * @summary JLS 4.10: subtype/supertype are binary relations on types.
 *          Subtyping does not extend through parameterized types: T <: S does
 *          not imply C<T> <: C<S>.
 * @run main Subtyping
 */

import java.util.List;
import java.util.ArrayList;

public class Subtyping {
    public static void main(String[] args) {
        // String <: Object.
        Object o = "str";
        if (o == null) throw new AssertionError("String is a subtype of Object");

        // A subtype value is assignable to a supertype variable.
        List<String> ls = new ArrayList<String>();
        // List<String> is NOT a subtype of List<Object> (checked in a separate
        // @compile/fail test), but it IS a subtype of List<? extends Object>.
        List<? extends Object> lw = ls;
        if (lw != ls) throw new AssertionError("List<String> <: List<? extends Object>");

        // The null type is a subtype of every reference type.
        String s = null;
        if (s != null) throw new AssertionError("null <: String");

        // Proper subtype: T < S iff T <: S and S != T.
        if (String.class.equals(Object.class)) throw new AssertionError("String != Object");

        System.out.println("PASS");
    }
}
