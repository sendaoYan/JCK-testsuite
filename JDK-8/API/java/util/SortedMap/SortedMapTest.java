/*
 * @test
 * @summary java.util.SortedMap API conformance: comparator (natural null vs custom), firstKey/
 *          lastKey with empty NoSuchElementException, headMap/tailMap/subMap range views
 *          (exclusive/inclusive), subMap(from>to) IllegalArgumentException, null-key NPE under
 *          natural ordering, and backed view liveness.
 * @run main SortedMapTest
 */

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class SortedMapTest {
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

    static void testNaturalOrder() {
        SortedMap<Integer, String> m = new TreeMap<>();
        m.put(3, "c");
        m.put(1, "a");
        m.put(2, "b");
        check(m.comparator() == null, "natural ordering comparator is null");
        check(m.firstKey() == 1 && m.lastKey() == 3, "firstKey/lastKey");
        check("b".equals(m.get(2)), "get by key");
        check(m.size() == 3, "size");
        expectThrows(NullPointerException.class, () -> m.put(null, "n"), "put(null) NPE natural");
    }

    static void testEmpty() {
        SortedMap<Integer, String> m = new TreeMap<>();
        expectThrows(NoSuchElementException.class, m::firstKey, "firstKey empty NSEE");
        expectThrows(NoSuchElementException.class, m::lastKey, "lastKey empty NSEE");
    }

    static void testCustomComparator() {
        SortedMap<Integer, String> m = new TreeMap<>(Comparator.<Integer>reverseOrder());
        m.put(1, "a");
        m.put(2, "b");
        check(m.comparator() != null, "custom comparator is non-null");
        check(m.firstKey() == 2, "reverse comparator firstKey");
    }

    static void testRangeViews() {
        SortedMap<Integer, String> m = new TreeMap<>();
        for (int i = 1; i <= 5; i++) m.put(i, "" + i);
        check(m.headMap(3).keySet().equals(new TreeSet<>(Arrays.asList(1, 2))), "headMap exclusive");
        check(m.tailMap(3).keySet().equals(new TreeSet<>(Arrays.asList(3, 4, 5))), "tailMap inclusive");
        check(m.subMap(2, 4).keySet().equals(new TreeSet<>(Arrays.asList(2, 3))), "subMap [from,to)");
        expectThrows(IllegalArgumentException.class, () -> m.subMap(4, 2), "subMap from>to IAE");
    }

    static void testBackedViews() {
        SortedMap<Integer, String> m = new TreeMap<>();
        m.put(1, "a");
        m.put(2, "b");
        SortedMap<Integer, String> sub = m.subMap(1, 4);
        sub.put(3, "c");
        check(m.containsKey(3), "subMap is live (put reflects)");
        check(m.keySet().size() == 3 && m.values().size() == 3 && m.entrySet().size() == 3,
                "keySet/values/entrySet sizes");
        sub.remove(2);
        check(!m.containsKey(2), "subMap remove reflects in backing map");
    }

    public static void main(String[] args) {
        testNaturalOrder();
        testEmpty();
        testCustomComparator();
        testRangeViews();
        testBackedViews();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
