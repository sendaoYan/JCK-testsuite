/*
 * @test
 * @summary java.util.concurrent.ConcurrentSkipListMap conformance: white-box + black-box
 *          (sorted map navigation firstKey/lastKey/ceiling/floor/higher/lower, subMap/
 *          headMap/tailMap/descendingMap, pollFirstEntry/pollLastEntry, comparator,
 *          null rejection, default-method compute/merge).
 * @run main ConcurrentSkipListMapTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiFunction;
import java.util.function.Function;

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
    static void testSortedNavigation() {
        ConcurrentSkipListMap<Integer, String> m = new ConcurrentSkipListMap<>();
        m.put(3, "c");
        m.put(1, "a");
        m.put(5, "e");
        m.put(2, "b");
        check(m.firstKey() == 1 && m.lastKey() == 5, "firstKey/lastKey");
        check("b".equals(m.get(2)), "get");
        check(m.ceilingKey(2) == 2 && m.ceilingKey(4) == 5, "ceilingKey");
        check(m.floorKey(4) == 3 && m.floorKey(2) == 2, "floorKey");
        check(m.higherKey(2) == 3 && m.lowerKey(2) == 1, "higherKey/lowerKey");
        check(m.higherKey(5) == null && m.lowerKey(1) == null, "higher/lower at bounds null");
        check(m.firstEntry().getKey() == 1 && m.lastEntry().getKey() == 5, "firstEntry/lastEntry");
        check(m.size() == 4, "size");
    }

    static void testSubMaps() {
        ConcurrentSkipListMap<Integer, String> m = new ConcurrentSkipListMap<>();
        for (int i = 1; i <= 10; i++) m.put(i, "v" + i);
        NavigableMap<Integer, String> sub = m.subMap(3, true, 7, false);
        check(sub.firstKey() == 3 && sub.lastKey() == 6, "subMap bounds inclusive/exclusive");
        check(sub.size() == 4, "subMap size");
        NavigableMap<Integer, String> head = m.headMap(4);
        check(head.size() == 3 && head.lastKey() == 3, "headMap exclusive");
        NavigableMap<Integer, String> tail = m.tailMap(8);
        check(tail.size() == 3 && tail.firstKey() == 8, "tailMap inclusive");
    }

    static void testDescending() {
        ConcurrentSkipListMap<Integer, String> m = new ConcurrentSkipListMap<>();
        m.put(1, "a");
        m.put(2, "b");
        m.put(3, "c");
        ConcurrentNavigableMap<Integer, String> d = m.descendingMap();
        check(d.firstKey() == 3 && d.lastKey() == 1, "descendingMap reversed");
        check(m.descendingKeySet().first() == 3, "descendingKeySet first");
    }

    static void testPollEntries() {
        ConcurrentSkipListMap<Integer, String> m = new ConcurrentSkipListMap<>();
        m.put(1, "a");
        m.put(2, "b");
        m.put(3, "c");
        check(m.pollFirstEntry().getKey() == 1, "pollFirstEntry");
        check(m.pollLastEntry().getKey() == 3, "pollLastEntry");
        check(m.firstKey() == 2 && m.size() == 1, "remaining entry");
    }

    static void testComparator() {
        ConcurrentSkipListMap<Integer, String> natural = new ConcurrentSkipListMap<>();
        check(natural.comparator() == null, "natural order comparator null");
        ConcurrentSkipListMap<Integer, String> rev = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        check(rev.comparator() != null, "explicit comparator non-null");
        rev.put(1, "a");
        rev.put(2, "b");
        check(rev.firstKey() == 2 && rev.lastKey() == 1, "reverse order firstKey/lastKey");
    }

    static void testNullRejection() {
        ConcurrentSkipListMap<Integer, String> m = new ConcurrentSkipListMap<>();
        expectThrows(() -> m.put(null, "x"), NullPointerException.class, "put(null key)");
        expectThrows(() -> m.put(1, null), NullPointerException.class, "put(null value)");
        expectThrows(() -> m.get(null), NullPointerException.class, "get(null)");
        expectThrows(() -> m.containsKey(null), NullPointerException.class, "containsKey(null)");
    }

    static void testDefaultMethods() {
        ConcurrentSkipListMap<String, Integer> m = new ConcurrentSkipListMap<>();
        m.put("a", 1);
        check(m.putIfAbsent("a", 2) == 1 && m.putIfAbsent("b", 2) == null, "putIfAbsent");
        check(m.computeIfAbsent("c", String::length) == 1, "computeIfAbsent");
        check(m.compute("a", (k, v) -> v + 1) == 2, "compute");
        check(m.merge("b", 10, Integer::sum) == 12, "merge combines");
        check(m.replace("a", 2, 20) && m.get("a") == 20, "replace(K,old,new)");
    }

    static void testBlackBox() {
        // constructors (Map)/(SortedMap) + isEmpty
        java.util.HashMap<Integer, String> hm = new java.util.HashMap<>();
        hm.put(1, "a");
        hm.put(2, "b");
        ConcurrentSkipListMap<Integer, String> m1 = new ConcurrentSkipListMap<>(hm);
        check(m1.size() == 2 && m1.get(1).equals("a"), "constructor(Map) copies");
        java.util.TreeMap<Integer, String> tm = new java.util.TreeMap<>();
        tm.put(3, "c");
        tm.put(1, "a");
        ConcurrentSkipListMap<Integer, String> m2 = new ConcurrentSkipListMap<>(tm);
        check(m2.firstKey() == 1 && m2.get(3).equals("c"), "constructor(SortedMap) keeps order");
        check(new ConcurrentSkipListMap<Integer, String>().isEmpty(), "isEmpty initially");

        ConcurrentSkipListMap<Integer, String> m = new ConcurrentSkipListMap<>();
        m.put(1, "a");
        m.put(3, "c");
        m.put(5, "e");

        // putFirst / putLast (SequencedMap) always throw UnsupportedOperationException
        expectThrows(() -> m.putFirst(0, "zero"), UnsupportedOperationException.class,
                     "putFirst throws UnsupportedOperationException");
        expectThrows(() -> m.putLast(6, "six"), UnsupportedOperationException.class,
                     "putLast throws UnsupportedOperationException");

        // ceilingEntry/floorEntry/higherEntry/lowerEntry (entry versions)
        Map.Entry<Integer, String> ce = m.ceilingEntry(4);
        check(ce.getKey() == 5 && "e".equals(ce.getValue()), "ceilingEntry");
        check(m.floorEntry(4).getKey() == 3, "floorEntry");
        check(m.higherEntry(3).getKey() == 5, "higherEntry");
        check(m.lowerEntry(3).getKey() == 1, "lowerEntry");
        check(m.ceilingEntry(99) == null && m.lowerEntry(0) == null, "boundary entries null");

        // containsValue / getOrDefault
        check(m.containsValue("c") && !m.containsValue("zzz"), "containsValue");
        check("c".equals(m.getOrDefault(3, "default")), "getOrDefault present");
        check("default".equals(m.getOrDefault(99, "default")), "getOrDefault missing");

        // computeIfPresent
        check("a!".equals(m.computeIfPresent(1, (k, v) -> v + "!")), "computeIfPresent present");
        check(m.computeIfPresent(99, (k, v) -> "x") == null && !m.containsKey(99),
              "computeIfPresent absent no-op");

        // replaceAll
        m.replaceAll((k, v) -> v.toUpperCase());
        check("C".equals(m.get(3)), "replaceAll maps values");

        // remove(Object)/remove(Object,Object)
        check("A!".equals(m.remove(1)), "remove(Object) returns value");
        check(!m.containsKey(1), "remove(Object) applied");
        check(!m.remove(3, "WRONG"), "remove(K,V) non-matching false");
        check(m.remove(3, "C"), "remove(K,V) matching true");

        // views: entrySet/keySet/navigableKeySet/values
        m.clear();
        m.put(2, "b");
        m.put(1, "a");
        check(m.entrySet().size() == 2, "entrySet size");
        check(m.keySet().size() == 2 && m.keySet().contains(1), "keySet");
        java.util.NavigableSet<Integer> nks = m.navigableKeySet();
        check(nks.first() == 1 && nks.last() == 2, "navigableKeySet order");
        check(m.values().size() == 2 && m.values().contains("a"), "values");

        // forEach(BiConsumer)
        java.util.ArrayList<String> seen = new java.util.ArrayList<>();
        m.forEach((k, v) -> seen.add(k + "=" + v));
        seen.sort(String::compareTo);
        check(seen.toString().equals("[1=a, 2=b]"), "forEach(BiConsumer)");

        // clone() (covariant and Object return types)
        ConcurrentSkipListMap<Integer, String> copy = m.clone();
        check(copy.equals(m) && copy != m, "clone() equals but distinct");
        check(copy.firstKey() == 1 && copy.lastKey() == 2, "clone() preserves order");
        Object oCopy = m.clone();
        check(oCopy instanceof ConcurrentSkipListMap, "clone() Object return type");

        // clear
        m.clear();
        check(m.isEmpty(), "clear empties");
    }

    // ---- appended: contract deepening ----
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

    static ConcurrentSkipListMap<Integer, String> fixedMap() {
        ConcurrentSkipListMap<Integer, String> m = new ConcurrentSkipListMap<>();
        m.put(10, "v10");
        m.put(20, "v20");
        m.put(30, "v30");
        m.put(40, "v40");
        return m;
    }

    static void testNullFullRejection() {
        ConcurrentSkipListMap<Integer, String> m = new ConcurrentSkipListMap<>();
        m.put(1, "a");
        expectThrows(() -> m.put(2, null), NullPointerException.class, "put(null value)");
        expectThrows(() -> m.getOrDefault(null, "d"), NullPointerException.class, "getOrDefault(null,d)");
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

    static void testNavigationMatrix() {
        ConcurrentSkipListMap<Integer, String> m = fixedMap();
        check(m.ceilingKey(10) == 10, "ceilingKey equal");
        check(m.ceilingKey(11) == 20, "ceilingKey between");
        check(m.ceilingKey(41) == null && m.ceilingKey(99) == null, "ceilingKey above all null");
        check(m.floorKey(10) == 10, "floorKey equal");
        check(m.floorKey(19) == 10, "floorKey between");
        check(m.floorKey(9) == null, "floorKey below all null");
        check(m.higherKey(10) == 20, "higherKey equal-key");
        check(m.higherKey(35) == 40, "higherKey between");
        check(m.higherKey(40) == null, "higherKey last null");
        check(m.lowerKey(10) == null, "lowerKey first null");
        check(m.lowerKey(20) == 10, "lowerKey equal-key");
        check(m.lowerKey(25) == 20, "lowerKey between");
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
        // firstEntry is a point-in-time snapshot unaffected by later same-key put
        Map.Entry<Integer, String> fe = m.firstEntry();
        m.put(20, "CHANGED");
        check("v20".equals(fe.getValue()), "firstEntry snapshot unaffected by later put");
        check("CHANGED".equals(m.get(20)), "map itself sees the change");
        ConcurrentSkipListMap<Integer, String> e = new ConcurrentSkipListMap<>();
        check(e.pollFirstEntry() == null && e.pollLastEntry() == null, "poll on empty null");
        expectThrows(() -> e.firstKey(), java.util.NoSuchElementException.class, "firstKey empty NSEE");
        expectThrows(() -> e.lastKey(), java.util.NoSuchElementException.class, "lastKey empty NSEE");
        check(e.firstEntry() == null && e.lastEntry() == null, "first/lastEntry empty null");
    }

    static void testSubMapsFull() {
        ConcurrentSkipListMap<Integer, String> m = fixedMap();
        NavigableMap<Integer, String> sub = m.subMap(10, true, 40, true);
        check(sub.size() == 4, "subMap inclusive both");
        check(m.subMap(10, false, 40, false).size() == 2, "subMap exclusive both");
        check(m.subMap(15, true, 35, false).size() == 2, "subMap mixed edges");
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
        java.util.ArrayList<Integer> order = new java.util.ArrayList<>();
        for (Integer k : d.keySet()) order.add(k);
        check(order.equals(java.util.Arrays.asList(40, 30, 20, 10)), "descendingMap iteration reversed");
        d.remove(20);
        check(!m.containsKey(20), "descendingMap remove writes through");
        m.put(20, "v20");
        java.util.ArrayList<Integer> dks = new java.util.ArrayList<>();
        for (Integer k : m.descendingKeySet()) dks.add(k);
        check(dks.equals(java.util.Arrays.asList(40, 30, 20, 10)), "descendingKeySet order");
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
        java.util.Iterator<Map.Entry<Integer, String>> it = m.entrySet().iterator();
        it.next();
        it.remove();
        check(m.size() == 3, "entrySet iterator remove writes through");
        expectThrows(() -> m.entrySet().iterator().remove(), IllegalStateException.class,
                     "entrySet iterator.remove before next throws IllegalStateException");
        ConcurrentSkipListMap<Integer, String> a = fixedMap();
        java.util.ArrayList<Integer> keys = new java.util.ArrayList<>();
        for (Map.Entry<Integer, String> e : a.entrySet()) keys.add(e.getKey());
        check(keys.equals(java.util.Arrays.asList(10, 20, 30, 40)), "entrySet ascending order");
        check(a.keySet().toString().equals("[10, 20, 30, 40]"), "keySet ascending order");
        java.util.ArrayList<String> vals = new java.util.ArrayList<>();
        for (String v : a.values()) vals.add(v);
        check(vals.equals(java.util.Arrays.asList("v10", "v20", "v30", "v40")), "values ascending order");
    }

    static void testComputeMergeBranches() {
        ConcurrentSkipListMap<String, Integer> m = new ConcurrentSkipListMap<>();
        final int[] calls = {0};
        check(m.computeIfAbsent("b", k -> null) == null && !m.containsKey("b"),
              "cIA null result leaves no entry");
        check(m.computeIfPresent("a", (k, v) -> v + 1) == null && !m.containsKey("a"),
              "cIP absent no-op");
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
        check(m.replace(99, "x") == null, "replace(K,V) absent returns null");
        check(!m.replace(30, "WRONG", "x"), "replace(K,old,new) mismatch false");
        check(m.get(30).equals("v30"), "mismatched replace leaves value");
        check(m.replace(30, "v30", "v30b"), "replace(K,old,new) match true");
        check(!m.replace(99, "any", "x"), "replace(K,old,new) absent false");
        check(m.remove(20, "new"), "remove(K,V) match true");
        check(!m.remove(30, "WRONG"), "remove(K,V) mismatch false");
        check(m.containsKey(30), "mismatched remove keeps entry");
    }

    static void testEqualsTreeMapExact() {
        ConcurrentSkipListMap<Integer, String> m = fixedMap();
        java.util.TreeMap<Integer, String> tm = new java.util.TreeMap<>(m);
        check(m.equals(tm) && tm.equals(m), "cross-impl equals with TreeMap");
        check(m.hashCode() == tm.hashCode(), "hashCode matches Map contract");
        check(m.toString().equals("{10=v10, 20=v20, 30=v30, 40=v40}"),
              "toString ascending deterministic");
        check(!m.equals(null), "equals(null) false");
    }

    static void testSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ConcurrentSkipListMap.class).getSerialVersionUID()
              == -8627078645895051609L, "serialVersionUID locked");
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
        // natural order round-trip stays mutable
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
        testSortedNavigation();
        testSubMaps();
        testDescending();
        testPollEntries();
        testComparator();
        testNullRejection();
        testDefaultMethods();
        testBlackBox();
        testNullFullRejection();
        testNavigationMatrix();
        testPollEntryContract();
        testSubMapsFull();
        testDescendingContract();
        testViewsWriteThrough();
        testComputeMergeBranches();
        testReplacePrecise();
        testEqualsTreeMapExact();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
