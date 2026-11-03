/*
 * @test
 * @summary java.util.NavigableMap conformance: white-box structure + black-box behavior.
 * @run main NavigableMapTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class NavigableMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.NavigableMap");
        check(c.isInterface(), "is an interface");
        check(java.util.SortedMap.class.isAssignableFrom(c), "extends SortedMap");
        for (String m : new String[] {"lowerEntry", "lowerKey", "floorEntry", "floorKey",
                "ceilingEntry", "ceilingKey", "higherEntry", "higherKey"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m, Object.class).getModifiers()),
                    m + " abstract");
        for (String m : new String[] {"firstEntry", "lastEntry", "pollFirstEntry",
                "pollLastEntry", "descendingMap", "navigableKeySet", "descendingKeySet"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " abstract");
        check(!Modifier.isAbstract(c.getDeclaredMethod("reversed").getModifiers()), "reversed default");
        check(Modifier.isAbstract(c.getDeclaredMethod("lowerEntry", Object.class).getModifiers()),
                "lowerEntry abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. nearest-key queries (lower/floor/ceiling/higher, key and entry forms)
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

        // 2. first/last entry + key
        check(m.firstEntry().getKey() == 1 && m.lastEntry().getKey() == 5, "firstEntry/lastEntry");
        check(m.firstKey() == 1 && m.lastKey() == 5, "firstKey/lastKey");

        // 3. pollFirstEntry/pollLastEntry remove and return
        check("a".equals(m.pollFirstEntry().getValue()), "pollFirstEntry");
        check("e".equals(m.pollLastEntry().getValue()), "pollLastEntry");
        check(m.size() == 1 && m.firstKey() == 3, "size after polls");

        // 4. descendingMap / descendingKeySet / navigableKeySet
        NavigableMap<Integer, String> d = new TreeMap<>();
        d.put(1, "a");
        d.put(2, "b");
        d.put(3, "c");
        check(d.descendingMap().firstKey() == 3 && d.descendingMap().lastKey() == 1,
                "descendingMap reverses order");
        check(d.descendingKeySet().first() == 3, "descendingKeySet");
        check(d.navigableKeySet().first() == 1 && d.navigableKeySet().last() == 3,
                "navigableKeySet");

        // 5. range views (inclusive-exclusive boolean variants)
        NavigableMap<Integer, String> r = new TreeMap<>();
        for (int i = 1; i <= 6; i++) r.put(i, "" + i);
        check(r.subMap(2, true, 4, true).keySet().equals(java.util.Set.of(2, 3, 4)),
                "subMap inclusive");
        check(r.headMap(3, false).keySet().equals(java.util.Set.of(1, 2)), "headMap exclusive");
        check(r.tailMap(4, false).keySet().equals(java.util.Set.of(5, 6)), "tailMap exclusive");
        check(r.subMap(2, 4).keySet().equals(java.util.Set.of(2, 3)), "subMap [from,to)");

        // 6. reversed() view
        NavigableMap<Integer, String> rev = r.reversed();
        check(rev.firstKey() == 6 && rev.lastKey() == 1, "reversed view");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
