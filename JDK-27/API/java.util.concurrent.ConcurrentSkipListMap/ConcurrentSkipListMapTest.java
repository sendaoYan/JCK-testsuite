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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSortedNavigation();
        testSubMaps();
        testDescending();
        testPollEntries();
        testComparator();
        testNullRejection();
        testDefaultMethods();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
