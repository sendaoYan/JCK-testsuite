/*
 * @test
 * @summary java.util.concurrent.ConcurrentNavigableMap conformance: white-box structure + black-box behavior.
 * @run main ConcurrentNavigableMapTest
 */

import java.util.*;
import java.util.concurrent.*;

public class ConcurrentNavigableMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.ConcurrentNavigableMap");
        check(c.isInterface(), "ConcurrentNavigableMap is an interface");
        check(ConcurrentMap.class.isAssignableFrom(c), "extends ConcurrentMap");
        check(NavigableMap.class.isAssignableFrom(c), "extends NavigableMap");
        hasMethod(c, "subMap", Object.class, boolean.class, Object.class, boolean.class);
        hasMethod(c, "headMap", Object.class, boolean.class);
        hasMethod(c, "tailMap", Object.class, boolean.class);
        hasMethod(c, "descendingMap");
        hasMethod(c, "navigableKeySet");
        hasMethod(c, "descendingKeySet");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ConcurrentNavigableMap<Integer, String> m = new ConcurrentSkipListMap<>();
        m.put(1, "a");
        m.put(3, "c");
        m.put(5, "e");
        check("a".equals(m.firstEntry().getValue()), "firstEntry");
        check("e".equals(m.lastEntry().getValue()), "lastEntry");
        check(m.ceilingKey(2) == 3, "ceilingKey");
        check(m.floorKey(4) == 3, "floorKey");
        check(m.subMap(1, 5).size() == 2, "subMap(1,5) size 2");
        check(m.headMap(3).size() == 1, "headMap(3) size 1");
        check(m.tailMap(3).size() == 2, "tailMap(3) size 2");
        check(m.descendingMap().firstKey() == 5, "descendingMap firstKey is max");
        NavigableSet<Integer> ks = m.navigableKeySet();
        check(ks.first() == 1 && ks.last() == 5, "navigableKeySet ordered");
        check(m.descendingKeySet().first() == 5, "descendingKeySet");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
