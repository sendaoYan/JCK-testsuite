/*
 * @test
 * @summary JLS 4.7 (Java SE 8): a reifiable type is one whose type information
 *          is fully available at run time (primitive types, non-generic or raw
 *          types, unbounded-wildcard parameterizations, and array types whose
 *          element type is reifiable). instanceof may not use a non-reifiable
 *          type, but reifiable types are fine.
 * @run main ReifiableTypes
 */

import java.util.List;

public class ReifiableTypes {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // Primitive types are reifiable.
        check(int.class != null, "int is reifiable");

        // Non-generic and raw types are reifiable (instanceof is allowed).
        Object s = "abc";
        check(s instanceof String, "String is reifiable");
        List raw = new java.util.ArrayList();
        check(raw instanceof List, "raw List is reifiable");

        // An unbounded-wildcard parameterization is reifiable.
        List<?> wild = new java.util.ArrayList<String>();
        check(wild instanceof List<?>, "List<?> is reifiable");

        // Array types whose element type is reifiable are reifiable.
        Object arr = new String[1];
        check(arr instanceof String[], "String[] is reifiable");
        check(arr instanceof Object[], "Object[] is reifiable");
        check(arr instanceof java.io.Serializable, "arrays implement Serializable (reifiable)");
        check(arr instanceof Cloneable, "arrays implement Cloneable (reifiable)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
