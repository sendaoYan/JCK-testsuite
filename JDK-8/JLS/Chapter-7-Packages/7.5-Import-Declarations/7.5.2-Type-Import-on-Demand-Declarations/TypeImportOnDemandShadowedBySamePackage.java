/*
 * @test
 * @summary JLS 7.5.2 (Java SE 8): a type declared in the same package shadows a
 *          type-import-on-demand declaration of a type with the same simple
 *          name (JLS 7.6 note / Example 7.6-1).
 * @run main test.TypeImportOnDemandShadowedBySamePackage
 */
package test;

import java.util.*;

public class TypeImportOnDemandShadowedBySamePackage {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Vector v = new Vector();   // test.Vector, not java.util.Vector
        check("test.Vector".equals(v.getClass().getName()),
              "same-package type shadows on-demand import");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class Vector {
    int[] vec;
}
