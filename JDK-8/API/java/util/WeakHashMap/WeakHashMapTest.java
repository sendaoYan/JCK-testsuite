/*
 * @test
 * @summary java.util.WeakHashMap API conformance: basic map operations.
 * @run main WeakHashMapTest
 */

import java.util.WeakHashMap;

public class WeakHashMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWeakHashMap() {
        WeakHashMap<String, Integer> m = new WeakHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        check(m.size() == 2, "size");
        check(m.get("a") == 1, "get");
        check(m.containsKey("b"), "containsKey");
        check(m.remove("a") == 1, "remove");
        check(m.size() == 1, "size after remove");
    }

    public static void main(String[] args) {
        testWeakHashMap();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
