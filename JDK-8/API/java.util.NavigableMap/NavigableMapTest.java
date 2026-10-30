/*
 * @test
 * @summary java.util.NavigableMap API conformance: lowerKey/floorKey/ceilingKey/higherKey
 *          (with out-of-range nulls), pollFirstEntry/pollLastEntry, and descendingMap.
 * @run main NavigableMapTest
 */

import java.util.NavigableMap;
import java.util.TreeMap;

public class NavigableMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNearest() {
        NavigableMap<Integer, String> m = new TreeMap<>();
        m.put(1, "a");
        m.put(3, "c");
        m.put(5, "e");
        check(m.lowerKey(3) == 1, "lowerKey");
        check(m.floorKey(3) == 3, "floorKey");
        check(m.ceilingKey(3) == 3, "ceilingKey");
        check(m.higherKey(3) == 5, "higherKey");
        check(m.ceilingKey(6) == null && m.lowerKey(1) == null, "out-of-range nulls");
    }

    static void testPollAndDescending() {
        NavigableMap<Integer, String> m = new TreeMap<>();
        m.put(1, "a");
        m.put(3, "c");
        m.put(5, "e");
        check("a".equals(m.pollFirstEntry().getValue()), "pollFirstEntry");
        check("e".equals(m.pollLastEntry().getValue()), "pollLastEntry");
        check(m.firstKey() == 3 && m.size() == 1, "size after polls");

        NavigableMap<Integer, String> d = new TreeMap<>();
        d.put(1, "a");
        d.put(2, "b");
        d.put(3, "c");
        check(d.descendingMap().firstKey() == 3, "descendingMap");
    }

    public static void main(String[] args) {
        testNearest();
        testPollAndDescending();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
