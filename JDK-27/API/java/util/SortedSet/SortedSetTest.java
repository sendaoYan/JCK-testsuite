/*
 * @test
 * @summary java.util.SortedSet conformance: white-box structure + black-box behavior.
 * @run main SortedSetTest
 */

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class SortedSetTest {
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
        Class<?> c = Class.forName("java.util.SortedSet");
        check(c.isInterface(), "is an interface");
        check(java.util.Set.class.isAssignableFrom(c), "extends Set");
        for (String m : new String[] {"comparator", "first", "last"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("subSet", Object.class, Object.class)
                .getModifiers()), "subSet abstract");
        for (String m : new String[] {"headSet", "tailSet"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m, Object.class).getModifiers()),
                    m + " abstract");
        check(!Modifier.isAbstract(c.getDeclaredMethod("addFirst", Object.class).getModifiers()),
                "addFirst default");
    }

    static void testBlackBox() {
        SortedSet<Integer> s = new TreeSet<>();
        s.add(1);
        s.add(2);
        s.add(3);
        check(s.comparator() == null, "natural comparator null");
        check(s.first() == 1 && s.last() == 3, "first/last");
        check(s.subSet(1, 3).equals(java.util.Set.of(1, 2)), "subSet");
        check(s.headSet(2).equals(java.util.Set.of(1)), "headSet");
        check(s.tailSet(2).equals(java.util.Set.of(2, 3)), "tailSet");
        check(s.getFirst() == 1 && s.getLast() == 3, "getFirst/getLast");
        expectThrows(() -> s.addFirst(0), UnsupportedOperationException.class, "addFirst UOE");
        check(s.reversed().first() == 3, "reversed");
        check(s.spliterator().estimateSize() == 3, "spliterator");

        SortedSet<Integer> rev = new TreeSet<>(Comparator.<Integer>reverseOrder());
        rev.add(1);
        rev.add(2);
        check(rev.first() == 2, "reverse comparator first");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
