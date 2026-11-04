/*
 * @test
 * @summary java.util.TreeMap API conformance: full contract coverage.
 *          natural/comparator ordering, put/get/remove, first/last (key and entry),
 *          NavigableMap ops (lower/floor/ceiling/higher key+entry, pollFirst/pollLast),
 *          subMap/headMap/tailMap range views (inclusive/exclusive + write-through),
 *          descendingMap/descendingKeySet/navigableKeySet live views, constructor matrix
 *          and exception paths, null key/value semantics, fail-fast iterators (CME),
 *          Entry.setValue old-value return, Map default methods deep null branches and
 *          function side-effect contracts, keySet/entrySet/values toArray contract,
 *          cross-implementation equals/hashCode, clone (shallow + structural independence),
 *          serialization round-trip + serialVersionUID, and state invariants.
 * @run main TreeMapTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class TreeMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static TreeMap<Integer, String> filled(int n) {
        TreeMap<Integer, String> m = new TreeMap<>();
        for (int i = 1; i <= n; i++) m.put(i, "v" + i);
        return m;
    }

    @SuppressWarnings("unchecked")
    static <K, V> TreeMap<K, V> roundTrip(TreeMap<K, V> map) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(map); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (TreeMap<K, V>) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testNaturalOrder() {
        TreeMap<String, Integer> map = new TreeMap<>();
        check(map.comparator() == null, "natural ordering comparator null");
        map.put("c", 3);
        map.put("a", 1);
        map.put("b", 2);
        check(map.firstKey().equals("a") && map.lastKey().equals("c"), "firstKey/lastKey");
        check(map.firstEntry().getValue() == 1 && map.lastEntry().getValue() == 3,
              "firstEntry/lastEntry");
        check(map.size() == 3 && map.get("b") == 2, "size/get");
        check(map.put("a", 10) == 1 && map.get("a") == 10, "put existing returns old");
        check(map.remove("b") == 2 && !map.containsKey("b"), "remove");
    }

    static void testComparator() {
        TreeMap<Integer, String> map = new TreeMap<>(Comparator.reverseOrder());
        map.put(1, "one");
        map.put(3, "three");
        map.put(2, "two");
        check(map.firstKey() == 3 && map.lastKey() == 1, "reverse comparator order");
    }

    static void testNavigable() {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (int i = 1; i <= 10; i += 2) {
            map.put(i, "v" + i); // 1,3,5,7,9
        }
        check(map.lowerKey(5) == 3, "lowerKey");
        check(map.floorKey(5) == 5 && map.floorKey(4) == 3, "floorKey");
        check(map.ceilingKey(5) == 5 && map.ceilingKey(4) == 5, "ceilingKey");
        check(map.higherKey(5) == 7, "higherKey");

        check(map.pollFirstEntry().getKey() == 1, "pollFirstEntry");
        check(map.pollLastEntry().getKey() == 9, "pollLastEntry");
        check(map.size() == 3, "size after polls");
    }

    static void testRangeViews() {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (int i = 1; i <= 10; i++) {
            map.put(i, "v" + i);
        }
        check(map.subMap(3, 7).size() == 4 && map.subMap(3, 7).firstKey() == 3
              && map.subMap(3, 7).lastKey() == 6, "subMap fromKey inclusive, toKey exclusive");
        check(map.headMap(4).size() == 3, "headMap");
        check(map.tailMap(8).size() == 3, "tailMap");
        check(map.subMap(3, true, 7, true).size() == 5, "subMap inclusive bounds");
    }

    // ---- constructor matrix + exception paths -------------------------------

    static void testConstructorMatrix() {
        checkThrows(NullPointerException.class, () -> new TreeMap<Integer, String>((Map<Integer, String>) null),
                    "TreeMap(null Map)");
        checkThrows(NullPointerException.class, () -> new TreeMap<Integer, String>((SortedMap<Integer, String>) null),
                    "TreeMap(null SortedMap)");
        checkThrows(NullPointerException.class, () -> new TreeMap<Integer, String>((Comparator<Integer>) null).put(null, "x"),
                    "TreeMap(null Comparator) put(null key) uses natural ordering");

        // TreeMap(Map) with natural ordering rejects null keys.
        Map<Integer, String> withNullKey = new HashMap<>();
        withNullKey.put(null, "n");
        checkThrows(NullPointerException.class, () -> new TreeMap<>(withNullKey),
                    "TreeMap(Map) with null key (natural order) throws NPE");

        // copy from SortedMap preserves comparator.
        SortedMap<Integer, String> rev = new TreeMap<>(Comparator.reverseOrder());
        rev.put(1, "a");
        rev.put(2, "b");
        TreeMap<Integer, String> fromSorted = new TreeMap<>(rev);
        check(fromSorted.comparator() == Comparator.<Integer>reverseOrder(), "TreeMap(SortedMap) keeps comparator");
        check(fromSorted.firstKey() == 2 && fromSorted.lastKey() == 1, "TreeMap(SortedMap) keeps order");

        // comparator that tolerates null permits null key (and get/containsKey(null) work).
        TreeMap<Integer, String> nullTolerant = new TreeMap<>(Comparator.nullsFirst(Comparator.naturalOrder()));
        nullTolerant.put(null, "root");
        check(nullTolerant.containsKey(null) && nullTolerant.get(null).equals("root"),
              "null key allowed with null-tolerant comparator");
        check(nullTolerant.firstKey() == null, "null key is first with nullsFirst");
    }

    static void testNullSemantics() {
        TreeMap<Integer, String> m = new TreeMap<>();
        checkThrows(NullPointerException.class, () -> m.put(null, "x"), "put null key (natural)");
        checkThrows(NullPointerException.class, () -> m.get(null), "get(null) natural NPE");
        checkThrows(NullPointerException.class, () -> m.containsKey(null), "containsKey(null) natural NPE");
        checkThrows(NullPointerException.class, () -> m.remove(null), "remove(null) natural NPE");
        checkThrows(NullPointerException.class, () -> m.computeIfAbsent(null, k -> "v"), "computeIfAbsent null key NPE");

        m.put(1, null);
        check(m.containsKey(1) && m.get(1) == null, "null value allowed");
        check(m.containsValue(null), "containsValue(null) true for mapped-to-null");
        check(!m.containsValue("nope"), "containsValue false for absent value");

        // comparator that throws on null also rejects null key.
        TreeMap<Integer, String> cmp = new TreeMap<>(Comparator.naturalOrder());
        checkThrows(NullPointerException.class, () -> cmp.put(null, "x"), "comparator put null key NPE");
    }

    // ---- NavigableMap boundary + live views ---------------------------------

    static void testNavigableBoundaries() {
        TreeMap<Integer, String> empty = new TreeMap<>();
        checkThrows(NoSuchElementException.class, empty::firstKey, "empty firstKey NSEE");
        checkThrows(NoSuchElementException.class, empty::lastKey, "empty lastKey NSEE");
        check(empty.firstEntry() == null && empty.lastEntry() == null, "empty first/lastEntry null");
        check(empty.pollFirstEntry() == null && empty.pollLastEntry() == null, "empty poll entry null");
        check(empty.lowerKey(5) == null && empty.floorKey(5) == null, "empty lower/floor null");
        check(empty.ceilingKey(5) == null && empty.higherKey(5) == null, "empty ceiling/higher null");
        check(empty.lowerEntry(5) == null && empty.higherEntry(5) == null, "empty lower/higherEntry null");

        TreeMap<Integer, String> m = filled(10);
        check(m.lowerKey(1) == null && m.higherKey(10) == null, "boundary lower/higher null");
        check(m.floorEntry(5).getKey() == 5 && m.ceilingEntry(5).getKey() == 5, "floor/ceilingEntry exact");
        check(m.lowerEntry(3).getKey() == 2 && m.higherEntry(9).getKey() == 10, "lower/higherEntry neighbors");
        check(m.floorKey(0) == null && m.ceilingKey(11) == null, "out of range floor/ceiling null");
    }

    static void testRangeViewWriteThrough() {
        TreeMap<Integer, String> m = filled(10);
        SortedMap<Integer, String> sub = m.subMap(3, 7);
        checkThrows(IllegalArgumentException.class, () -> sub.put(100, "out"), "subMap put out of range IAE");
        checkThrows(IllegalArgumentException.class, () -> sub.put(2, "out"), "subMap put below range IAE");
        sub.put(5, "five");
        check(m.get(5).equals("five"), "subMap put reflects in map");
        sub.remove(4);
        check(!m.containsKey(4), "subMap remove reflects in map");
        sub.clear();
        check(!m.containsKey(3) && !m.containsKey(6), "subMap clear removes range only");
        check(m.containsKey(2) && m.containsKey(7), "elements outside range survive clear");

        checkThrows(IllegalArgumentException.class, () -> m.subMap(7, 3), "subMap from>to IAE");
        checkThrows(IllegalArgumentException.class, () -> m.subMap(3, true, 7, false).put(7, "x"),
                    "subMap exclusive toKey rejects put(toKey)");
        checkThrows(IllegalArgumentException.class, () -> m.subMap(3, false, 7, true).put(3, "x"),
                    "subMap exclusive fromKey rejects put(fromKey)");

        // headMap/tailMap views are live and bounded.
        SortedMap<Integer, String> head = m.headMap(4);
        checkThrows(IllegalArgumentException.class, () -> head.put(4, "x"), "headMap put at/above bound IAE");
        head.put(3, "three");
        check(m.get(3).equals("three"), "headMap put reflects");
        SortedMap<Integer, String> tail = m.tailMap(8);
        tail.put(8, "eight");
        check(m.get(8).equals("eight"), "tailMap put reflects");
    }

    static void testDescendingNavigableViews() {
        TreeMap<Integer, String> m = filled(10);
        NavigableMap<Integer, String> desc = m.descendingMap();
        check(desc.firstKey() == 10 && desc.lastKey() == 1, "descendingMap endpoints");
        check(desc.comparator().compare(5, 3) < 0, "descendingMap comparator reverses order");
        desc.put(11, "eleven");
        check(m.containsKey(11) && m.firstKey() == 1 && m.lastKey() == 11, "descendingMap put reflects");
        desc.remove(1);
        check(!m.containsKey(1), "descendingMap remove reflects");

        NavigableSet<Integer> dks = m.descendingKeySet();
        check(dks.first() == 11 && dks.last() == 2, "descendingKeySet endpoints");
        dks.remove(11);
        check(!m.containsKey(11), "descendingKeySet remove reflects");

        NavigableSet<Integer> nks = m.navigableKeySet();
        check(nks.first() == 2 && nks.last() == 10, "navigableKeySet endpoints");
        nks.pollFirst();
        check(!m.containsKey(2), "navigableKeySet pollFirst reflects");
        nks.pollLast();
        check(!m.containsKey(10), "navigableKeySet pollLast reflects");
    }

    // ---- keySet/values/entrySet view write channels -------------------------

    static void testViewWriteChannels() {
        TreeMap<Integer, String> m = filled(5);
        Set<Integer> keys = m.keySet();
        checkThrows(UnsupportedOperationException.class, () -> keys.add(9), "keySet.add UOE");
        check(keys.remove(2) && !m.containsKey(2), "keySet.remove reflects");
        keys.removeAll(Arrays.asList(3, 4));
        check(!m.containsKey(3) && !m.containsKey(4), "keySet.removeAll reflects");
        keys.clear();
        check(m.isEmpty(), "keySet.clear empties map");

        TreeMap<Integer, String> mv = filled(5);
        mv.values().remove("v2");
        check(!mv.containsKey(2), "values.remove reflects");
        checkThrows(UnsupportedOperationException.class, () -> mv.values().add("x"), "values.add UOE");

        TreeMap<Integer, String> me = filled(5);
        Set<Map.Entry<Integer, String>> entries = me.entrySet();
        checkThrows(UnsupportedOperationException.class, () -> entries.add(null), "entrySet.add UOE");
        Iterator<Map.Entry<Integer, String>> it = entries.iterator();
        it.next();
        it.remove();
        check(me.size() == 4, "entrySet iterator.remove reflects");
        entries.removeAll(entries);
        check(me.isEmpty(), "entrySet.removeAll(itself) empties");
    }

    static void testEntrySetValue() {
        TreeMap<Integer, String> m = filled(3);
        Map.Entry<Integer, String> e = m.entrySet().iterator().next();
        check(e.setValue("NEW").equals("v1"), "setValue returns old value");
        check(m.get(1).equals("NEW"), "setValue writes through to map");
        check(e.setValue(null).equals("NEW"), "setValue(null) returns old value (null value allowed)");
        check(m.get(1) == null, "setValue(null) writes null through");
        m.put(1, "reset");

        // firstEntry/lastEntry/lowerEntry/... return immutable snapshots: setValue -> UOE.
        checkThrows(UnsupportedOperationException.class, () -> m.firstEntry().setValue("x"),
                    "firstEntry snapshot setValue UOE");
        checkThrows(UnsupportedOperationException.class, () -> m.lastEntry().setValue("x"),
                    "lastEntry snapshot setValue UOE");
        checkThrows(UnsupportedOperationException.class, () -> m.lowerEntry(3).setValue("x"),
                    "lowerEntry snapshot setValue UOE");
    }

    static void testFailFast() {
        TreeMap<Integer, String> m = filled(5);
        Iterator<Integer> ki = m.keySet().iterator();
        ki.next();
        m.put(99, "x");
        checkThrows(java.util.ConcurrentModificationException.class, ki::next, "keySet iterator CME after put");

        Iterator<Map.Entry<Integer, String>> ei = m.entrySet().iterator();
        ei.next();
        m.remove(99);
        checkThrows(java.util.ConcurrentModificationException.class, ei::next, "entrySet iterator CME after remove");

        // value replacement is not structural; no CME.
        TreeMap<Integer, String> m2 = filled(3);
        Iterator<Integer> it = m2.keySet().iterator();
        it.next();
        m2.put(1, "changed"); // replace existing key
        check(it.next() == 2, "existing-key put does not invalidate iterator");
    }

    // ---- default methods deep branches --------------------------------------

    static void testDefaultMethods() {
        TreeMap<Integer, String> m = new TreeMap<>();
        check(m.putIfAbsent(1, "a") == null, "putIfAbsent absent inserts");
        check(m.putIfAbsent(1, "z").equals("a"), "putIfAbsent present returns existing");
        check(m.getOrDefault(1, "d").equals("a"), "getOrDefault present");
        check(m.getOrDefault(2, "d").equals("d"), "getOrDefault absent returns default");
        check(m.replace(1, "A").equals("a") && m.get(1).equals("A"), "replace(K,V)");
        check(m.replace(1, "A", "B") && m.get(1).equals("B"), "replace(K,old,new) matching");
        check(!m.replace(1, "wrong", "X"), "replace non-matching returns false");
        m.replaceAll((k, v) -> v + k);
        check(m.get(1).equals("B1"), "replaceAll transforms in place");

        // compute / computeIfAbsent / computeIfPresent / merge with side-effect counters.
        final int[] calls = {0};
        TreeMap<Integer, String> c = new TreeMap<>();
        check(c.computeIfAbsent(1, k -> { calls[0]++; return "one"; }).equals("one"), "computeIfAbsent computes");
        check(calls[0] == 1, "computeIfAbsent called exactly once");
        check(c.computeIfAbsent(1, k -> { calls[0]++; return "again"; }).equals("one"), "computeIfAbsent present skips function");
        check(calls[0] == 1, "computeIfAbsent not called when present");
        check(c.computeIfAbsent(2, k -> null) == null && !c.containsKey(2),
              "computeIfAbsent null result does not insert");

        check(c.computeIfPresent(1, (k, v) -> v + "!").equals("one!"), "computeIfPresent computes");
        check(c.computeIfPresent(9, (k, v) -> "x") == null && !c.containsKey(9), "computeIfPresent absent no-op");
        check(c.computeIfPresent(1, (k, v) -> null) == null && !c.containsKey(1),
              "computeIfPresent null result removes entry");

        c.put(5, "five");
        check(c.compute(5, (k, v) -> v + "!").equals("five!"), "compute on present");
        check(c.compute(6, (k, v) -> v == null ? "six" : "x").equals("six"), "compute on absent (v==null)");
        check(c.compute(5, (k, v) -> null) == null && !c.containsKey(5), "compute null result removes");

        check(c.merge(10, "ten", (o, n) -> o + n).equals("ten"), "merge absent inserts value");
        check(c.merge(10, "!", (o, n) -> o + n).equals("ten!"), "merge present combines");
        check(c.merge(6, "!", (o, n) -> null) == null && !c.containsKey(6), "merge null result removes");

        // exception passthrough + no side effect.
        final RuntimeException boom = new RuntimeException("boom");
        TreeMap<Integer, String> e = new TreeMap<>();
        e.put(1, "a");
        checkThrows(RuntimeException.class, () -> e.compute(1, (k, v) -> { throw boom; }), "compute exception propagates");
        check(e.get(1).equals("a"), "compute exception leaves mapping intact");
        final RuntimeException boom2 = new RuntimeException("boom2");
        checkThrows(RuntimeException.class, () -> e.computeIfAbsent(9, k -> { throw boom2; }), "computeIfAbsent exception propagates");
        check(!e.containsKey(9), "computeIfAbsent exception leaves no entry");
    }

    static void testForEachOrdering() {
        TreeMap<Integer, String> m = filled(5);
        final List<Integer> seen = new ArrayList<>();
        m.forEach((k, v) -> seen.add(k));
        check(seen.equals(Arrays.asList(1, 2, 3, 4, 5)), "forEach in ascending key order");
        checkThrows(NullPointerException.class, () -> m.forEach(null), "forEach(null) NPE");
    }

    // ---- toArray contract ---------------------------------------------------

    static void testToArrayContract() {
        TreeMap<Integer, String> m = filled(3);
        Set<Integer> keys = m.keySet();
        Integer[] exact = keys.toArray(new Integer[0]);
        check(exact.length == 3, "toArray(new T[0]) exact length");
        Integer[] reusable = new Integer[]{9, 9, 9, 9};
        Integer[] returned = keys.toArray(reusable);
        check(returned == reusable, "toArray(T[]) reuses array when it fits");
        check(reusable[3] == null, "toArray(T[]) pads trailing null");
        check(reusable[0] == 1 && reusable[1] == 2 && reusable[2] == 3, "toArray(T[]) content ascending");

        Integer[] small = keys.toArray(new Integer[0]);
        List<Integer> sorted = Arrays.asList(small.clone());
        java.util.Collections.sort(sorted);
        check(sorted.equals(Arrays.asList(1, 2, 3)), "keySet toArray multiset");

        // entrySet toArray with null terminator.
        Map.Entry<Integer, String>[] entries = (Map.Entry<Integer, String>[]) m.entrySet().toArray(new Map.Entry[5]);
        check(entries.length == 5 && entries[3] == null, "entrySet toArray(T[]) pads null");
        check(entries[0].getKey() == 1 && entries[1].getKey() == 2 && entries[2].getKey() == 3,
              "entrySet toArray content ascending");
    }

    // ---- cross-implementation equals/hashCode -------------------------------

    static void testCrossImplEquals() {
        TreeMap<Integer, String> tm = new TreeMap<>();
        tm.put(1, "a");
        tm.put(2, "b");
        tm.put(3, "c");

        HashMap<Integer, String> hm = new HashMap<>();
        hm.put(3, "c");
        hm.put(1, "a");
        hm.put(2, "b");
        check(tm.equals(hm) && hm.equals(tm), "TreeMap equals HashMap symmetric");
        check(tm.hashCode() == hm.hashCode(), "equal maps equal hashCode");

        LinkedHashMap<Integer, String> lhm = new LinkedHashMap<>();
        lhm.put(1, "a");
        lhm.put(2, "b");
        lhm.put(3, "c");
        check(tm.equals(lhm) && lhm.equals(tm), "TreeMap equals LinkedHashMap symmetric");
        check(tm.hashCode() == lhm.hashCode(), "TreeMap/LinkedHashMap equal hashCode");

        // two TreeMaps with different comparators but same content are equal.
        TreeMap<Integer, String> rev = new TreeMap<>(Comparator.reverseOrder());
        rev.put(1, "a");
        rev.put(2, "b");
        rev.put(3, "c");
        check(tm.equals(rev) && rev.equals(tm), "TreeMap equals regardless of comparator");

        hm.put(4, "d");
        check(!tm.equals(hm), "unequal after adding entry");
    }

    // ---- clone / serialization / state invariants ---------------------------

    static void testClone() {
        TreeMap<Integer, String> m = filled(3);
        @SuppressWarnings("unchecked")
        TreeMap<Integer, String> clone = (TreeMap<Integer, String>) m.clone();
        check(clone != m, "clone distinct object");
        check(clone.equals(m) && clone.hashCode() == m.hashCode(), "clone equals original");
        check(clone.comparator() == m.comparator(), "clone preserves comparator");
        clone.put(4, "v4");
        check(m.size() == 3 && !m.containsKey(4), "clone structural independence");
        // shallow copy: same value references.
        check(clone.get(1) == m.get(1), "clone is shallow (same value refs)");
    }

    static void testSerialization() throws Exception {
        TreeMap<Integer, String> m = filled(3);
        m.put(4, null); // null value survives round-trip
        TreeMap<Integer, String> r = roundTrip(m);
        check(r != m, "round-trip distinct object");
        check(r.equals(m), "round-trip content equal");
        check(r.get(4) == null && r.containsKey(4), "round-trip preserves null value");
        check(r.firstKey() == 1 && r.lastKey() == 4, "round-trip preserves ordering");
        r.put(5, "five");
        check(r.size() == 5 && m.size() == 4, "round-trip result usable and independent");

        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(TreeMap.class);
        check(osc.getSerialVersionUID() == 919286545866124006L, "TreeMap serialVersionUID stable");
    }

    static void testStateInvariants() {
        TreeMap<Integer, String> m = filled(5);
        m.remove(3);
        check(m.size() == 4 && !m.containsKey(3), "remove updates size");
        check(m.containsKey(1) && m.containsKey(2) && m.containsKey(4) && m.containsKey(5),
              "other entries survive remove");
        checkThrows(NullPointerException.class, () -> m.put(null, "x"), "failed put leaves state intact");
        check(m.size() == 4 && m.firstKey() == 1 && m.lastKey() == 5, "state intact after failed op");
        m.clear();
        check(m.isEmpty() && m.size() == 0, "clear empties");
        for (int i = 1; i <= 5; i++) check(!m.containsKey(i), "clear removed key " + i);
    }

    public static void main(String[] args) throws Exception {
        testNaturalOrder();
        testComparator();
        testNavigable();
        testRangeViews();
        testConstructorMatrix();
        testNullSemantics();
        testNavigableBoundaries();
        testRangeViewWriteThrough();
        testDescendingNavigableViews();
        testViewWriteChannels();
        testEntrySetValue();
        testFailFast();
        testDefaultMethods();
        testForEachOrdering();
        testToArrayContract();
        testCrossImplEquals();
        testClone();
        testSerialization();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
