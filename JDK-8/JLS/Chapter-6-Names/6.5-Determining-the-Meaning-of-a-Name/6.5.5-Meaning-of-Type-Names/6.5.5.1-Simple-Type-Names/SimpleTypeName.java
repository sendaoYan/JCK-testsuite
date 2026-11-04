/*
 * @test
 * @summary JLS 6.5.5.1 (Java SE 8): a simple type name denotes a visible type in
 *          scope, reachable from the same compilation unit, by a single-type
 *          import, or by a type-import-on-demand declaration.
 * @run main SimpleTypeName
 */

import java.util.Map;   // single-type-import
import java.util.*;     // type-import-on-demand

class SamePkgType { int v() { return 1; } }

public class SimpleTypeName {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        Map<String, String> m = new java.util.HashMap<String, String>();
        check(m != null, "single-type-import");

        List<String> l = new ArrayList<String>();
        check(l != null, "type-import-on-demand");

        SamePkgType t = new SamePkgType();
        check(t.v() == 1, "same compilation unit top-level type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
