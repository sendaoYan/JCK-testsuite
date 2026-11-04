/*
 * @test
 * @summary java.util.NavigableSet conformance: white-box structure + black-box behavior.
 * @run main NavigableSetTest
 */

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

public class NavigableSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.NavigableSet");
        check(c.isInterface(), "is an interface");
        check(SortedSet.class.isAssignableFrom(c), "extends SortedSet");
        for (String m : new String[] {"lower", "floor", "ceiling", "higher"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m, Object.class).getModifiers()),
                    m + " abstract");
        for (String m : new String[] {"pollFirst", "pollLast", "descendingSet",
                "descendingIterator"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " abstract");
    }

    static void testBlackBox() {
        NavigableSet<Integer> s = new TreeSet<>();
        s.add(1);
        s.add(3);
        s.add(5);
        check(s.lower(3) == 1 && s.floor(3) == 3, "lower/floor");
        check(s.ceiling(3) == 3 && s.higher(3) == 5, "ceiling/higher");
        check(s.lower(1) == null && s.higher(5) == null, "out-of-range nulls");
        check(s.first() == 1 && s.last() == 5, "first/last");
        check(s.pollFirst() == 1 && s.pollLast() == 5, "pollFirst/pollLast");
        check(s.size() == 1 && s.first() == 3, "size after polls");

        NavigableSet<Integer> d = new TreeSet<>();
        d.add(1);
        d.add(2);
        d.add(3);
        check(d.descendingSet().first() == 3 && d.descendingSet().last() == 1,
                "descendingSet");
        Iterator<Integer> it = d.descendingIterator();
        check(it.next() == 3 && it.next() == 2 && it.next() == 1, "descendingIterator");

        NavigableSet<Integer> r = new TreeSet<>();
        for (int i = 1; i <= 6; i++) r.add(i);
        check(r.subSet(2, true, 4, true).equals(java.util.Set.of(2, 3, 4)), "subSet inclusive");
        check(r.headSet(3, false).equals(java.util.Set.of(1, 2)), "headSet exclusive");
        check(r.tailSet(4, false).equals(java.util.Set.of(5, 6)), "tailSet exclusive");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
