/*
 * @test
 * @summary java.util.IdentityHashMap API conformance: reference-identity key semantics
 *          (get/containsKey/remove), size, and distinct-equal keys.
 * @run main IdentityHashMapTest
 */

import java.util.IdentityHashMap;

public class IdentityHashMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIdentitySemantics() {
        IdentityHashMap<String, String> m = new IdentityHashMap<>();
        String k1 = new String("x");
        String k2 = new String("x");
        m.put(k1, "v1");
        m.put(k2, "v2");
        check(m.size() == 2, "two distinct equal strings are two keys");
        check("v1".equals(m.get(k1)) && "v2".equals(m.get(k2)), "get by identity");
        check(m.get("x") == null, "get by equal-but-not-identical key returns null");
        check(m.containsKey(k1) && !m.containsKey(new String("x")), "containsKey identity");
        m.remove(k1);
        check(m.size() == 1 && !m.containsKey(k1), "remove by identity");
    }

    public static void main(String[] args) {
        testIdentitySemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
