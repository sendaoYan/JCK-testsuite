/*
 * @test
 * @summary java.util.concurrent.ConcurrentSkipListMap conformance: white-box + black-box
 *          (full sorted navigation matrix, headMap/tailMap/subMap two/four-arg with
 *          out-of-range IAE, descendingMap, poll entries with immutable Entry.setValue,
 *          null full rejection, compute/merge branch counters, serialization with and
 *          without Serializable comparator, cross-impl equals with TreeMap).
 * @run main ConcurrentSkipListMapTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ConcurrentSkipListMapTest {
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

    static void expectThrowsMsg(Runnable r, Class<? extends Throwable> type, String fragment,
                                String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                                         + " but got " + t.getClass().getSimpleName(), t);
            check(t.getMessage() != null && t.getMessage().contains(fragment),
                  msg + " message contains '" + fragment + "' (got: " + t.getMessage() + ")");
            passed++;
        }
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.ConcurrentSkipListMap");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractMap.class, "extends AbstractMap");
        check(ConcurrentNavigableMap.class.isAssignableFrom(c), "implements ConcurrentNavigableMap");
        check(Cloneable.class.isAssignableFrom(c) && Serializable.class.isAssignableFrom(c),
              "implements Cloneable/Serializable");
        check(c.getConstructor() != null && c.getConstructor(Comparator.class) != null,
              "constructors ()/(Comparator)");
        check(hasMethod(c, "firstKey") && hasMethod(c, "lastKey"), "firstKey/lastKey");
        check(hasMethod(c, "ceilingKey", Object.class) && hasMethod(c, "floorKey", Object.class)
              && hasMethod(c, "higherKey", Object.class) && hasMethod(c, "lowerKey", Object.class),
              "ceiling/floor/higher/lower key");
        check(hasMethod(c, "subMap", Object.class, boolean.class, Object.class, boolean.class),
              "subMap(K,boolean,K,boolean)");
        check(hasMethod(c, "headMap", Object.class, boolean.class)
              && hasMethod(c, "tailMap", Object.class, boolean.class), "headMap/tailMap");
        check(hasMethod(c, "descendingMap") && hasMethod(c, "descendingKeySet"),
              "descendingMap/descendingKeySet");
        check(hasMethod(c, "pollFirstEntry") && hasMethod(c, "pollLastEntry"),
              "pollFirstEntry/pollLastEntry");
        check(hasMethod(c, "comparator"), "comparator");
    }

    // ---- Part 2: black-box (behavioral) ----
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

    static void testNullFullRejection() {
        ConcurrentSkipListMap<Integer, String> m = new ConcurrentSkipListMap<>();
        m.put(1, "a");
        expectThrows(() -> m.put(null, "x"), NullPointerException.class, "put(null key)");
        expectThrows(() -> m.put(2, null), NullPointerException.class, "put(null value)");
        expectThrows(() -> m.get(null), NullPointerException.class, "get(null)");
        expectThrows(() -> m.getOrDefault(null, "d"), NullPointerException.class, "getOrDefault(null,d)");
        expectThrows(() -> m.containsKey(null), NullPointerException.class, "containsKey(null)");
        expectThrows(() -> m.containsValue(null), NullPointerException.class, "containsValue(null)");
        expectThrows(() -> m.remove(null), NullPointerException.class, "remove(null)");
        expectThrows(() -> m.remove(null, "x"), NullPointerException.class, "remove(null,v)");
        expectThrows(() -> m.putIfAbsent(null, "x"), NullPointerException.class, "putIfAbsent(null,v)");
        expectThrows(() -> m.putIfAbsent(2, null), NullPointerException.class, "putIfAbsent(k,null)");
        expectThrows(() -> m.merge(1, null, (a, b) -> a), NullPointerException.class, "merge(k,null v)");
        expectThrows(() -> m.computeIfAbsent(1, null), NullPointerException.class, "computeIfAbsent(k,null fn)");
        expectThrows(() -> m.computeIfPresent(1, null), NullPointerException.class,
                     "computeIfPresent(k,null fn, present key)");
        expectThrows(() -> m.putAll(null), NullPointerException.class, "putAll(null)");
        check(m.size() == 1 && m.get(1).equals("a"), "state intact after null rejections");
    }

    static ConcurrentSkipListMap<Integer, String> fixedMap() {
        ConcurrentSkipListMap<Integer, String> m = new ConcurrentSkipListMap<>();
        m.put(10, "v10");
        m.put(20, "v20");
        m.put(30, "v30");
        m.put(40, "v40");
        return m;
    }

    static void testNavigationMatrix() {
        ConcurrentSkipListMap<Integer, String> m = fixedMap();
        check(m.firstKey() == 10 && m.lastKey() == 40, "firstKey/lastKey");
        check(m.firstEntry().getKey() == 10 && "v10".equals(m.firstEntry().getValue()), "firstEntry");
        check(m.lastEntry().getKey() == 40 && "v40".equals(m.lastEntry().getValue()), "lastEntry");
        // ceiling: least key >= given
        check(m.ceilingKey(10) == 10, "ceilingKey equal");
        check(m.ceilingKey(11) == 20, "ceilingKey between");
        check(m.ceilingKey(40) == 40, "ceilingKey last");
        check(m.ceilingKey(41) == null && m.ceilingKey(99) == null, "ceilingKey above all null");
        // floor: greatest key <= given
        check(m.floorKey(10) == 10, "floorKey equal");
        check(m.floorKey(19) == 10, "floorKey between");
        check(m.floorKey(40) == 40, "floorKey last");
        check(m.floorKey(9) == null, "floorKey below all null");
        // higher: strictly greater
        check(m.higherKey(10) == 20, "higherKey equal-key");
        check(m.higherKey(35) == 40, "higherKey between");
        check(m.higherKey(40) == null, "higherKey last null");
        // lower: strictly less
        check(m.lowerKey(10) == null, "lowerKey first null");
        check(m.lowerKey(20) == 10, "lowerKey equal-key");
        check(m.lowerKey(25) == 20, "lowerKey between");
        // entry forms mirror key forms
        check(m.ceilingEntry(11).getKey() == 20 && "v20".equals(m.ceilingEntry(11).getValue()),
              "ceilingEntry");
        check(m.floorEntry(19).getKey() == 10, "floorEntry");
        check(m.higherEntry(30).getKey() == 40, "higherEntry");
        check(m.lowerEntry(30).getKey() == 20, "lowerEntry");
        check(m.ceilingEntry(99) == null && m.lowerEntry(5) == null, "entry boundary nulls");
    }

    static void testPollEntryContract() {
        ConcurrentSkipListMap<Integer, String> m = fixedMap();
        Map.Entry<Integer, String> p1 = m.pollFirstEntry();
        check(p1.getKey() == 10 && "v10".equals(p1.getValue()), "pollFirstEntry returns first");
        check(m.firstKey() == 20 && m.size() == 3, "pollFirstEntry removes from map");
        Map.Entry<Integer, String> p2 = m.pollLastEntry();
        check(p2.getKey() == 40, "pollLastEntry returns last");
        check(m.lastKey() == 30 && m.size() == 2, "pollLastEntry removes from map");
        // polled entries are immutable snapshots
        expectThrows(() -> p1.setValue("X"), UnsupportedOperationException.class,
                     "polled Entry.setValue throws UOE");
        m.put(50, "v50");
        check("v50".equals(m.lastEntry().getValue()), "sanity after re-put");
        // firstEntry/lastEntry are point-in-time snapshots: later same-key put does not
        // change a previously returned entry
        Map.Entry<Integer, String> fe = m.firstEntry();
        m.put(20, "CHANGED");
        check("v20".equals(fe.getValue()), "firstEntry snapshot unaffected by later put");
        check("CHANGED".equals(m.get(20)), "map itself sees the change");
        // empty map poll returns null
        ConcurrentSkipListMap<Integer, String> e = new ConcurrentSkipListMap<>();
        check(e.pollFirstEntry() == null && e.pollLastEntry() == null, "poll on empty null");
        expectThrows(() -> e.firstKey(), NoSuchElementException.class, "firstKey empty NSEE");
        expectThrows(() -> e.lastKey(), NoSuchElementException.class, "lastKey empty NSEE");
        check(e.firstEntry() == null && e.lastEntry() == null, "first/lastEntry empty null");
    }

    static void testSubMapsFull() {
        ConcurrentSkipListMap<Integer, String> m = fixedMap();
        // four-arg form with boolean edges
        NavigableMap<Integer, String> sub = m.subMap(10, true, 40, true);
        check(sub.size() == 4, "subMap inclusive both");
        check(m.subMap(10, false, 40, false).size() == 2, "subMap exclusive both");
        check(m.subMap(15, true, 35, false).size() == 2, "subMap mixed edges");
        NavigableMap<Integer, String> sub2 = m.subMap(20, true, 30, false);
        check(sub2.firstKey() == 20 && sub2.size() == 1, "subMap bounds and edges");
        // two-arg forms
        check(m.headMap(30).size() == 2, "headMap exclusive");
        check(m.headMap(30, true).size() == 3, "headMap inclusive");
        check(m.tailMap(30).size() == 2, "tailMap inclusive");
        check(m.tailMap(30, false).size() == 1, "tailMap exclusive");
        // views are live: parent puts appear in the view
        NavigableMap<Integer, String> head = m.headMap(30);
        m.put(5, "v5");
        check(head.containsKey(5) && head.size() == 3, "headMap live view sees parent put");
        m.remove(5);
        // view writes go through to the parent
        NavigableMap<Integer, String> tail = m.tailMap(30);
        tail.put(35, "v35");
        check(m.containsKey(35) && m.get(35).equals("v35"), "tailMap put writes through");
        tail.remove(35);
        check(!m.containsKey(35), "tailMap remove writes through");
        // out-of-range writes are rejected with "key out of range"
        expectThrowsMsg(() -> m.headMap(20).put(25, "x"), IllegalArgumentException.class,
                        "key out of range", "headMap put above bound");
        expectThrowsMsg(() -> m.tailMap(30).put(25, "x"), IllegalArgumentException.class,
                        "key out of range", "tailMap put below bound");
        expectThrowsMsg(() -> m.subMap(20, true, 30, true).put(35, "x"), IllegalArgumentException.class,
                        "key out of range", "subMap put above high bound");
        expectThrowsMsg(() -> m.subMap(20, true, 30, true).put(15, "x"), IllegalArgumentException.class,
                        "key out of range", "subMap put below low bound");
        check(m.size() == 4, "map intact after rejected out-of-range puts");
    }

    static void testDescendingContract() {
        ConcurrentSkipListMap<Integer, String> m = fixedMap();
        ConcurrentNavigableMap<Integer, String> d = m.descendingMap();
        check(d.firstKey() == 40 && d.lastKey() == 10, "descendingMap reversed bounds");
        check(d.size() == 4, "descendingMap size");
        List<Integer> order = new ArrayList<>();
        for (Integer k : d.keySet()) order.add(k);
        check(order.equals(Arrays.asList(40, 30, 20, 10)), "descendingMap iteration reversed");
        d.remove(20);
        check(!m.containsKey(20), "descendingMap remove writes through");
        m.put(20, "v20");
        // descendingKeySet
        List<Integer> dks = new ArrayList<>();
        for (Integer k : m.descendingKeySet()) dks.add(k);
        check(dks.equals(Arrays.asList(40, 30, 20, 10)), "descendingKeySet order");
        // descending of descending is the original order
        check(d.descendingMap().firstKey() == 10, "descendingMap twice restores order");
    }

    static void testViewsWriteThrough() {
        ConcurrentSkipListMap<Integer, String> m = fixedMap();
        m.navigableKeySet().remove(10);
        check(!m.containsKey(10) && m.size() == 3, "navigableKeySet remove writes through");
        m.values().remove("v20");
        check(!m.containsKey(20) && m.size() == 2, "values remove writes through");
        m.put(1, "v1");
        m.put(2, "v2");
        // entrySet iterator supports remove
        java.util.Iterator<Map.Entry<Integer, String>> it = m.entrySet().iterator();
        it.next();
        it.remove();
        check(m.size() == 3, "entrySet iterator remove writes through");
        expectThrows(() -> m.entrySet().iterator().remove(), IllegalStateException.class,
                     "entrySet iterator.remove before next throws IllegalStateException");
        // entrySet iterates ascending
        ConcurrentSkipListMap<Integer, String> a = fixedMap();
        List<Integer> keys = new ArrayList<>();
        for (Map.Entry<Integer, String> e : a.entrySet()) keys.add(e.getKey());
        check(keys.equals(Arrays.asList(10, 20, 30, 40)), "entrySet ascending order");
        check(a.keySet().toString().equals("[10, 20, 30, 40]"), "keySet ascending order");
        List<String> vals = new ArrayList<>();
        for (String v : a.values()) vals.add(v);
        check(vals.equals(Arrays.asList("v10", "v20", "v30", "v40")), "values ascending order");
    }

    static void testComputeMergeBranches() {
        ConcurrentSkipListMap<String, Integer> m = new ConcurrentSkipListMap<>();
        final int[] calls = {0};
        check(m.computeIfAbsent("a", k -> { calls[0]++; return 7; }) == 7, "cIA absent computes");
        check(calls[0] == 1, "cIA fn invoked once");
        check(m.computeIfAbsent("a", k -> { calls[0]++; return 99; }) == 7, "cIA present returns current");
        check(calls[0] == 1, "cIA fn not invoked when present");
        check(m.computeIfAbsent("b", k -> null) == null && !m.containsKey("b"),
              "cIA null result leaves no entry");
        check(m.computeIfPresent("zz", (k, v) -> 1) == null && !m.containsKey("zz"),
              "cIP absent no-op");
        check(m.computeIfPresent("a", (k, v) -> v + 1) == 8, "cIP present recomputes");
        check(m.computeIfPresent("a", (k, v) -> null) == null && !m.containsKey("a"),
              "cIP null removes");
        check(m.compute("n", (k, v) -> v == null ? 5 : -1) == 5, "compute absent inserts");
        check(m.compute("n", (k, v) -> v * 3) == 15, "compute present updates");
        check(m.compute("n", (k, v) -> null) == null && !m.containsKey("n"), "compute null removes");
        calls[0] = 0;
        check(m.merge("m", 3, (x, y) -> { calls[0]++; return x + y; }) == 3, "merge absent uses value");
        check(calls[0] == 0, "merge absent does not invoke fn");
        check(m.merge("m", 4, (x, y) -> { calls[0]++; return x * y; }) == 12, "merge present applies");
        check(calls[0] == 1, "merge present invokes fn once");
        check(m.merge("m", 5, (x, y) -> null) == null && !m.containsKey("m"), "merge null removes");
        // exception propagation with original object
        RuntimeException boom = new RuntimeException("boom");
        try {
            m.compute("boom", (k, v) -> { throw boom; });
            throw new AssertionError("compute exception must propagate");
        } catch (RuntimeException t) {
            check(t == boom, "compute propagates original exception");
        }
        check(!m.containsKey("boom"), "compute exception leaves no entry");
    }

    static void testReplacePrecise() {
        ConcurrentSkipListMap<Integer, String> m = fixedMap();
        check("v20".equals(m.replace(20, "new")), "replace(K,V) returns old value");
        check(m.get(20).equals("new"), "replace(K,V) updated");
        check(m.replace(99, "x") == null, "replace(K,V) absent returns null");
        check(!m.replace(30, "WRONG", "x"), "replace(K,old,new) mismatch false");
        check(m.get(30).equals("v30"), "mismatched replace leaves value");
        check(m.replace(30, "v30", "v30b"), "replace(K,old,new) match true");
        check(m.get(30).equals("v30b"), "matched replace updated");
        check(!m.replace(99, "any", "x"), "replace(K,old,new) absent false");
        check(m.remove(20, "new"), "remove(K,V) match true");
        check(!m.remove(30, "WRONG"), "remove(K,V) mismatch false");
        check(m.containsKey(30), "mismatched remove keeps entry");
    }

    static void testComparatorAndConstructors() {
        ConcurrentSkipListMap<Integer, String> natural = new ConcurrentSkipListMap<>();
        check(natural.comparator() == null, "natural order comparator null");
        natural.put(3, "c");
        natural.put(1, "a");
        natural.put(2, "b");
        check(natural.firstKey() == 1 && natural.lastKey() == 3, "natural order sorted");
        ConcurrentSkipListMap<Integer, String> rev = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        check(rev.comparator() != null, "explicit comparator non-null");
        rev.put(1, "a");
        rev.put(2, "b");
        rev.put(3, "c");
        check(rev.firstKey() == 3 && rev.lastKey() == 1, "reverse order bounds");
        List<Integer> order = new ArrayList<>();
        for (Integer k : rev.keySet()) order.add(k);
        check(order.equals(Arrays.asList(3, 2, 1)), "reverse order iteration");
        // ctor(Map) and ctor(SortedMap)
        java.util.HashMap<Integer, String> hm = new java.util.HashMap<>();
        hm.put(1, "a");
        hm.put(2, "b");
        ConcurrentSkipListMap<Integer, String> m1 = new ConcurrentSkipListMap<>(hm);
        check(m1.size() == 2 && m1.firstKey() == 1, "ctor(Map) copies and sorts");
        TreeMap<Integer, String> tm = new TreeMap<>(Comparator.reverseOrder());
        tm.put(3, "c");
        tm.put(1, "a");
        ConcurrentSkipListMap<Integer, String> m2 = new ConcurrentSkipListMap<>(tm);
        check(m2.firstKey() == 3 && m2.comparator() != null, "ctor(SortedMap) keeps comparator+order");
        expectThrows(() -> new ConcurrentSkipListMap<Integer, String>((Map<Integer, String>) null),
                     NullPointerException.class, "ctor(null Map)");
        check(new ConcurrentSkipListMap<Integer, String>().isEmpty(), "ctor() empty");
        // clone
        @SuppressWarnings("unchecked")
        ConcurrentSkipListMap<Integer, String> clone = (ConcurrentSkipListMap<Integer, String>) natural.clone();
        check(clone != natural && clone.equals(natural), "clone equals but distinct");
        clone.put(9, "i");
        check(!natural.containsKey(9), "clone structurally independent");
        // putAll
        ConcurrentSkipListMap<Integer, String> pa = new ConcurrentSkipListMap<>();
        pa.putAll(natural);
        check(pa.equals(natural), "putAll copies content");
    }

    static void testEqualsTreeMap() {
        ConcurrentSkipListMap<Integer, String> m = fixedMap();
        TreeMap<Integer, String> tm = new TreeMap<>(m);
        check(m.equals(tm) && tm.equals(m), "cross-impl equals with TreeMap");
        check(m.hashCode() == tm.hashCode(), "hashCode matches Map contract");
        tm.put(99, "x");
        check(!m.equals(tm), "different content not equal");
        check(!m.equals(null), "equals(null) false");
        String ts = m.toString();
        check(ts.equals("{10=v10, 20=v20, 30=v30, 40=v40}"), "toString ascending deterministic");
        m.clear();
        check(m.isEmpty() && m.size() == 0, "clear empties");
        check(m.toString().equals("{}"), "empty toString");
    }

    static void testForEachAndSearch() {
        ConcurrentSkipListMap<Integer, String> m = fixedMap();
        final long[] keySum = {0};
        m.forEach((k, v) -> keySum[0] += k);
        check(keySum[0] == 100, "forEach(BiConsumer) visits every key");
        List<String> visited = new ArrayList<>();
        m.forEach((k, v) -> visited.add(v));
        check(visited.equals(Arrays.asList("v10", "v20", "v30", "v40")),
              "forEach visits in ascending order");
        check(m.values().size() == 4, "values view size");
        check(m.keySet().containsAll(Arrays.asList(10, 20, 30, 40)), "keySet containsAll");
        check(m.entrySet().size() == 4, "entrySet size");
    }

    static void testSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ConcurrentSkipListMap.class).getSerialVersionUID()
              == -8627078645895051609L, "serialVersionUID locked");
        // natural order round-trip
        ConcurrentSkipListMap<Integer, String> m = fixedMap();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(m);
        }
        @SuppressWarnings("unchecked")
        ConcurrentSkipListMap<Integer, String> copy = (ConcurrentSkipListMap<Integer, String>)
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy.equals(m) && copy.firstKey() == 10, "natural comparator round-trip");
        check(copy.put(50, "v50") == null, "resurrected map mutable");
        // serializable comparator round-trip preserves order and comparator
        ConcurrentSkipListMap<Integer, String> rev = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        rev.put(1, "a");
        rev.put(2, "b");
        rev.put(3, "c");
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos2)) {
            oos.writeObject(rev);
        }
        @SuppressWarnings("unchecked")
        ConcurrentSkipListMap<Integer, String> revCopy = (ConcurrentSkipListMap<Integer, String>)
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos2.toByteArray())).readObject();
        check(revCopy.firstKey() == 3 && revCopy.lastKey() == 1, "reverse comparator round-trip order");
        check(revCopy.comparator() != null, "comparator survives round-trip");
        check(revCopy.equals(rev), "reverse comparator round-trip content");
        // a non-Serializable comparator makes the map non-serializable
        ConcurrentSkipListMap<Integer, String> lam = new ConcurrentSkipListMap<>((a, b) -> b - a);
        lam.put(1, "a");
        java.io.ByteArrayOutputStream bos3 = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos3)) {
            oos.writeObject(lam);
            check(false, "non-Serializable comparator must fail serialization");
        } catch (java.io.NotSerializableException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testMap();
        testNullRejection();
        testNullFullRejection();
        testNavigationMatrix();
        testPollEntryContract();
        testSubMapsFull();
        testDescendingContract();
        testViewsWriteThrough();
        testComputeMergeBranches();
        testReplacePrecise();
        testComparatorAndConstructors();
        testEqualsTreeMap();
        testForEachAndSearch();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
