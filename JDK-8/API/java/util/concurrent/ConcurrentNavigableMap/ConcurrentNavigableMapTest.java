/*
 * @test
 * @summary java.util.concurrent.ConcurrentNavigableMap API conformance: navigable ops.
 * @run main ConcurrentNavigableMapTest
 */

import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ConcurrentNavigableMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConcurrentNavigableMap() {
        ConcurrentNavigableMap<Integer, String> m = new ConcurrentSkipListMap<Integer, String>();
        m.put(1, "a");
        m.put(3, "c");
        m.put(5, "e");
        check("a".equals(m.firstEntry().getValue()), "firstEntry");
        check("e".equals(m.lastEntry().getValue()), "lastEntry");
        check(m.ceilingKey(2) == 3, "ceilingKey");
        check(m.floorKey(4) == 3, "floorKey");
        check(m.subMap(1, 5).size() == 2, "subMap");
    }

    public static void main(String[] args) {
        testConcurrentNavigableMap();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
