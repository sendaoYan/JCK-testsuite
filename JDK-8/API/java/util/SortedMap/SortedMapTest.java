/*
 * @test
 * @summary java.util.SortedMap API conformance: comparator (natural null vs custom), firstKey/
 *          lastKey with empty NoSuchElementException, headMap/tailMap/subMap range views
 *          (exclusive/inclusive), subMap(from>to) IllegalArgumentException, null-key NPE under
 *          natural ordering, and backed view liveness.
 * @run main SortedMapTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    static void testIterationOrderIsKeyOrder() {
        SortedMap<Integer, String> m = new TreeMap<>();
        int[] insert = {5, 1, 3, 2, 4};
        for (int k : insert) m.put(k, "v" + k);
        List<Integer> keys = new ArrayList<>();
        for (Integer k : m.keySet()) keys.add(k);
        check(keys.equals(Arrays.asList(1, 2, 3, 4, 5)), "keySet iteration is ascending key order");
        List<String> vals = new ArrayList<>();
        for (String v : m.values()) vals.add(v);
        check(vals.equals(Arrays.asList("v1", "v2", "v3", "v4", "v5")),
                "values iteration follows key order");
        List<Integer> entryKeys = new ArrayList<>();
        for (Map.Entry<Integer, String> e : m.entrySet()) entryKeys.add(e.getKey());
        check(entryKeys.equals(Arrays.asList(1, 2, 3, 4, 5)), "entrySet iteration is ascending key order");
        check(m.toString().equals("{1=v1, 2=v2, 3=v3, 4=v4, 5=v5}"), "toString in key order: " + m);
    }

    static void testRangeBoundariesExact() {
        SortedMap<Integer, String> m = new TreeMap<>();
        for (int i = 1; i <= 5; i++) m.put(i, "" + i);
        SortedMap<Integer, String> head = m.headMap(3);
        check(head.firstKey() == 1 && head.lastKey() == 2 && head.size() == 2,
                "headMap(3) = [1,3) endpoints exact");
        check(!head.containsKey(3) && head.containsKey(2), "headMap excludes toKey");
        SortedMap<Integer, String> tail = m.tailMap(3);
        check(tail.firstKey() == 3 && tail.lastKey() == 5 && tail.size() == 3,
                "tailMap(3) = [3,..) endpoints exact, inclusive");
        SortedMap<Integer, String> sub = m.subMap(2, 4);
        check(sub.firstKey() == 2 && sub.lastKey() == 3, "subMap(2,4) endpoints: from inclusive, to exclusive");
        check(sub.containsKey(2) && !sub.containsKey(4), "subMap boundary keys");

        // degenerate and out-of-range bounds
        check(m.subMap(3, 3).isEmpty(), "subMap(k,k) empty");
        check(m.headMap(1).isEmpty(), "headMap(minKey) empty");
        check(m.headMap(99).size() == 5, "headMap(above all) whole map");
        check(m.tailMap(99).isEmpty(), "tailMap(above all) empty");
        check(m.tailMap(0).size() == 5, "tailMap(below all) whole map");
        check(m.subMap(0, 99).size() == 5, "subMap(surrounding) whole map");
        expectThrows(NoSuchElementException.class, () -> m.subMap(3, 3).firstKey(),
                "empty subMap firstKey NSEE");

        // range views expose the same comparator instance
        Comparator<Integer> rev = Comparator.<Integer>reverseOrder();
        SortedMap<Integer, String> cm = new TreeMap<>(rev);
        cm.put(1, "a");
        cm.put(2, "b");
        check(cm.comparator() == rev, "comparator() returns the same instance");
        check(cm.headMap(2).comparator() == rev && cm.tailMap(2).comparator() == rev
                && cm.subMap(2, 1).comparator() == rev, "range views share comparator instance");
        // under reversed ordering the bounds swap: subMap(from=2, to=1) is the valid range
        check(cm.subMap(2, 1).size() == 1 && cm.subMap(2, 1).containsKey(2),
                "reversed comparator bounds follow comparator order");
        expectThrows(IllegalArgumentException.class, () -> cm.subMap(1, 2),
                "subMap bounds against comparator direction IAE");
    }

    static void testViewLivenessAndCME() {
        SortedMap<Integer, String> m = new TreeMap<>();
        for (int i = 1; i <= 5; i++) m.put(i, "v" + i);
        SortedMap<Integer, String> sub = m.subMap(2, 5); // {2,3,4}
        // mutation through parent inside range is visible in the view
        m.put(3, "new3");
        check(sub.get(3).equals("new3"), "parent value change visible in subMap");
        m.put(4, "new4");
        check(sub.size() == 3, "parent in-range put keeps subMap size (replace)");
        m.put(10, "ten");
        check(sub.size() == 3 && !sub.containsKey(10), "parent out-of-range put invisible to subMap");
        // view range methods track parent changes
        m.remove(2);
        check(!sub.containsKey(2) && sub.size() == 2, "parent remove shrinks subMap");
        check(sub.firstKey() == 3, "subMap firstKey advances after parent remove");
        m.put(1, "one");
        check(sub.firstKey() == 3, "parent below-range insert does not enter subMap");
        // fail-fast: structural parent modification invalidates view iterators
        Iterator<Integer> it = sub.keySet().iterator();
        it.next();
        m.put(99, "x");
        expectThrows(java.util.ConcurrentModificationException.class, it::next,
                "subMap iterator CME after parent structural modification");
        // clear through the view removes only the range
        sub.clear();
        check(!m.containsKey(3) && !m.containsKey(4), "subMap.clear removed range keys");
        check(m.containsKey(1) && m.containsKey(5) && m.containsKey(99),
                "keys outside range survive subMap.clear");
    }

    static void testEmptyMapRangeViews() {
        SortedMap<Integer, String> m = new TreeMap<>();
        check(m.headMap(5).isEmpty() && m.tailMap(5).isEmpty() && m.subMap(1, 2).isEmpty(),
                "empty map range views empty");
        check(!m.headMap(5).keySet().iterator().hasNext(), "empty view iterator exhausted");
        // views over an empty map are live: inserting through them populates the parent
        SortedMap<Integer, String> head = m.headMap(10);
        head.put(5, "five");
        check(m.size() == 1 && m.containsKey(5), "put through empty headMap view reaches parent");
        expectThrows(IllegalArgumentException.class, () -> head.put(10, "x"),
                "headMap view rejects put at bound");
    }

    static void testSortedMapEquality() {
        SortedMap<Integer, String> a = new TreeMap<>();
        a.put(1, "a");
        a.put(2, "b");
        Map<Integer, String> b = new java.util.HashMap<>();
        b.put(2, "b");
        b.put(1, "a");
        check(a.equals(b) && b.equals(a), "SortedMap equals Map with same entrySet");
        check(a.hashCode() == b.hashCode(), "SortedMap hashCode = entry sum");
        // subMap view equals an independently built map with the range content
        SortedMap<Integer, String> sub = a.subMap(1, 2);
        Map<Integer, String> one = new java.util.HashMap<>();
        one.put(1, "a");
        check(sub.equals(one) && one.equals(sub), "subMap equals standalone map of same content");
        // view of a reverse-ordered map still has ascending-by-comparator equality semantics
        SortedMap<Integer, String> rev = new TreeMap<>(Comparator.<Integer>reverseOrder());
        rev.put(1, "a");
        rev.put(2, "b");
        check(rev.equals(a) && a.equals(rev), "equality independent of comparator direction");
    }

    public static void main(String[] args) {
        testNaturalOrder();
        testEmpty();
        testCustomComparator();
        testRangeViews();
        testBackedViews();
        testIterationOrderIsKeyOrder();
        testRangeBoundariesExact();
        testViewLivenessAndCME();
        testEmptyMapRangeViews();
        testSortedMapEquality();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
