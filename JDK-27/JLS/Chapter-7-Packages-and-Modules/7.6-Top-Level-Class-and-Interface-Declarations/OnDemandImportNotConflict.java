/*
 * @test
 * @summary JLS 7.6 Example 7.6-1 note: declaring a top level class whose name
 *          collides with a type available via a type-import-on-demand is NOT a
 *          compile-time error; the local class takes precedence.
 * @run main OnDemandImportNotConflict
 */

import java.util.*;

class Vector {}

public class OnDemandImportNotConflict {
    public static void main(String[] args) {
        Vector v = new Vector();                 // the local Vector
        java.util.Vector<String> jv = new java.util.Vector<>();
        if (v == null) throw new AssertionError("local Vector");
        if (jv.size() != 0) throw new AssertionError("java.util.Vector");

        System.out.println("PASS");
    }
}
