/*
 * @test
 * @summary java.util.NavigableMap API conformance: lower/floor/ceiling/higher key and entry forms
 *          (with out-of-range nulls), firstEntry/lastEntry/pollFirstEntry/pollLastEntry,
 *          descendingMap/descendingKeySet/navigableKeySet, boolean-inclusive range views, subMap
 *          (from>to) IllegalArgumentException, and null-key NPE under natural ordering.
 * @run main NavigableMapTest
 */

import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class NavigableMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
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
        check("a".equals(m.lowerEntry(3).getValue()), "lowerEntry");
        check("e".equals(m.higherEntry(3).getValue()), "higherEntry");
        check("c".equals(m.floorEntry(3).getValue()), "floorEntry");
        check("c".equals(m.ceilingEntry(3).getValue()), "ceilingEntry");
        expectThrows(NullPointerException.class, () -> m.put(null, "n"), "put(null) NPE natural");
    }

    static void testEntries() {
        NavigableMap<Integer, String> m = new TreeMap<>();
        m.put(1, "a");
        m.put(3, "c");
        m.put(5, "e");
        check(m.firstEntry().getKey() == 1 && m.lastEntry().getKey() == 5, "firstEntry/lastEntry");
        check("a".equals(m.pollFirstEntry().getValue()), "pollFirstEntry");
        check("e".equals(m.pollLastEntry().getValue()), "pollLastEntry");
        check(m.size() == 1 && m.firstKey() == 3, "size after polls");

        NavigableMap<Integer, String> empty = new TreeMap<>();
        check(empty.firstEntry() == null && empty.lastEntry() == null, "firstEntry/lastEntry empty null");
        check(empty.pollFirstEntry() == null && empty.pollLastEntry() == null, "poll entries empty null");
    }

    static void testDescending() {
        NavigableMap<Integer, String> d = new TreeMap<>();
        d.put(1, "a");
        d.put(2, "b");
        d.put(3, "c");
        check(d.descendingMap().firstKey() == 3 && d.descendingMap().lastKey() == 1,
                "descendingMap reverses order");
        check(d.descendingKeySet().first() == 3 && d.descendingKeySet().last() == 1,
                "descendingKeySet");
        check(d.navigableKeySet().first() == 1 && d.navigableKeySet().last() == 3,
                "navigableKeySet");
    }

    static void testRangeViews() {
        NavigableMap<Integer, String> r = new TreeMap<>();
        for (int i = 1; i <= 6; i++) r.put(i, "" + i);
        check(r.subMap(2, true, 4, true).keySet().equals(new TreeSet<>(Arrays.asList(2, 3, 4))),
                "subMap inclusive");
        check(r.headMap(3, false).keySet().equals(new TreeSet<>(Arrays.asList(1, 2))),
                "headMap exclusive");
        check(r.tailMap(4, false).keySet().equals(new TreeSet<>(Arrays.asList(5, 6))),
                "tailMap exclusive");
        check(r.subMap(2, 4).keySet().equals(new TreeSet<>(Arrays.asList(2, 3))), "subMap [from,to)");
        expectThrows(IllegalArgumentException.class, () -> r.subMap(4, true, 2, true),
                "subMap from>to IAE");
    }

    public static void main(String[] args) {
        testNearest();
        testEntries();
        testDescending();
        testRangeViews();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
