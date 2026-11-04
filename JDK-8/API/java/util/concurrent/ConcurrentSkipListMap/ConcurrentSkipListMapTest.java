/*
 * @test
 * @summary java.util.concurrent.ConcurrentSkipListMap API conformance: sorted firstKey/lastKey,
 *          get/ceilingKey/higherKey, remove, and null key rejection.
 * @run main ConcurrentSkipListMapTest
 */

import java.util.concurrent.ConcurrentSkipListMap;

public class ConcurrentSkipListMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMap() {
        ConcurrentSkipListMap<Integer, String> m = new ConcurrentSkipListMap<>();
        m.put(3, "c");
        m.put(1, "a");
        m.put(2, "b");
        check(m.firstKey() == 1 && m.lastKey() == 3, "firstKey/lastKey");
        check("b".equals(m.get(2)), "get");
        check(m.ceilingKey(2) == 2 && m.higherKey(2) == 3, "ceilingKey/higherKey");
        check(m.size() == 3, "size");
        m.remove(2);
        check(!m.containsKey(2), "remove");
    }

    static void testNullRejection() {
        ConcurrentSkipListMap<Integer, String> m = new ConcurrentSkipListMap<>();
        try {
            m.put(null, "x");
            throw new AssertionError("put(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    public static void main(String[] args) {
        testMap();
        testNullRejection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
