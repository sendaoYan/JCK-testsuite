/*
 * @test
 * @summary java.util.SortedMap conformance: white-box structure + black-box behavior.
 * @run main SortedMapTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SequencedMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class SortedMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.SortedMap");
        check(c.isInterface(), "is an interface");
        check(SequencedMap.class.isAssignableFrom(c), "extends SequencedMap");
        for (String m : new String[] {"comparator", "firstKey", "lastKey", "keySet", "values",
                "entrySet"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("subMap", Object.class, Object.class)
                .getModifiers()), "subMap abstract");
        for (String m : new String[] {"headMap", "tailMap"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m, Object.class).getModifiers()),
                    m + " abstract");
        check(!Modifier.isAbstract(c.getDeclaredMethod("putFirst", Object.class, Object.class)
                .getModifiers()), "putFirst default");
    }

    static void testBlackBox() {
        SortedMap<Integer, String> m = new TreeMap<>();
        m.put(1, "a");
        m.put(2, "b");
        m.put(3, "c");
        check(m.comparator() == null, "natural comparator null");
        check(m.firstKey() == 1 && m.lastKey() == 3, "firstKey/lastKey");
        check(m.subMap(1, 3).keySet().equals(java.util.Set.of(1, 2)), "subMap");
        check(m.headMap(2).keySet().equals(java.util.Set.of(1)), "headMap");
        check(m.tailMap(2).keySet().equals(java.util.Set.of(2, 3)), "tailMap");
        check(m.keySet().equals(java.util.Set.of(1, 2, 3)), "keySet");
        check(m.values().contains("b") && m.values().size() == 3, "values");
        check(m.entrySet().size() == 3, "entrySet");
        expectThrows(() -> m.putFirst(0, "z"), UnsupportedOperationException.class, "putFirst UOE");
        check(m.reversed().firstKey() == 3, "reversed");

        SortedMap<Integer, String> rev = new TreeMap<>(Comparator.<Integer>reverseOrder());
        rev.put(1, "a");
        rev.put(2, "b");
        check(rev.firstKey() == 2, "reverse comparator firstKey");
    }

    static void testSequencedDefaults() {
        SortedMap<Integer, String> m = new TreeMap<>();
        m.put(1, "a");
        m.put(2, "b");
        m.put(3, "c");
        // SortedMap default putFirst/putLast are position-blind: always UOE
        expectThrows(() -> m.putFirst(0, "z"), UnsupportedOperationException.class,
                "putFirst UOE");
        expectThrows(() -> m.putLast(9, "z"), UnsupportedOperationException.class,
                "putLast UOE");
        check(m.size() == 3 && !m.containsKey(0) && !m.containsKey(9),
                "failed putFirst/putLast leave map unchanged");
        // SequencedMap entry defaults operate through firstKey/lastKey
        check(m.firstEntry().getKey() == 1 && m.firstEntry().getValue().equals("a"),
                "firstEntry default");
        check(m.lastEntry().getKey() == 3, "lastEntry default");
        check(m.pollFirstEntry().getKey() == 1, "pollFirstEntry removes first");
        check(m.size() == 2 && m.firstKey() == 2, "state after pollFirstEntry");
        check(m.pollLastEntry().getKey() == 3, "pollLastEntry removes last");
        check(m.size() == 1, "state after pollLastEntry");
        m.put(1, "a");
        m.put(3, "c");
        // reversed() is a live descending view
        SortedMap<Integer, String> r = m.reversed();
        check(r.firstKey() == 3 && r.lastKey() == 1, "reversed endpoints");
        java.util.List<Integer> rk = new java.util.ArrayList<>(r.keySet());
        check(rk.equals(java.util.Arrays.asList(3, 2, 1)), "reversed key order descending");
        r.put(4, "d");
        check(m.size() == 4 && m.lastKey() == 4, "reversed put reflects parent");
        r.remove(1);
        check(!m.containsKey(1), "reversed remove reflects parent");
        SortedMap<Integer, String> rr = r.reversed();
        java.util.List<Integer> rrk = new java.util.ArrayList<>(rr.keySet());
        check(rrk.equals(java.util.Arrays.asList(2, 3, 4)), "reversed().reversed() restores order");
        check(rr.firstKey() == 2, "double reversal endpoints");
        // empty map sequenced defaults
        SortedMap<Integer, String> e = new TreeMap<>();
        check(e.firstEntry() == null && e.lastEntry() == null, "empty first/lastEntry null");
        check(e.pollFirstEntry() == null && e.pollLastEntry() == null, "empty poll null");
        check(e.reversed().isEmpty(), "empty reversed empty");
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
        expectThrows(() -> m.subMap(3, 3).firstKey(), NoSuchElementException.class,
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
        expectThrows(() -> cm.subMap(1, 2), IllegalArgumentException.class,
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
        expectThrows(it::next, java.util.ConcurrentModificationException.class,
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
        expectThrows(() -> head.put(10, "x"), IllegalArgumentException.class,
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testIterationOrderIsKeyOrder();
        testRangeBoundariesExact();
        testViewLivenessAndCME();
        testEmptyMapRangeViews();
        testSortedMapEquality();
        testSequencedDefaults();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
