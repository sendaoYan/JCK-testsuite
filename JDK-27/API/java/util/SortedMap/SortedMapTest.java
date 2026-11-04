/*
 * @test
 * @summary java.util.SortedMap conformance: white-box structure + black-box behavior.
 * @run main SortedMapTest
 */

import java.lang.reflect.Modifier;
import java.util.Comparator;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
