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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
