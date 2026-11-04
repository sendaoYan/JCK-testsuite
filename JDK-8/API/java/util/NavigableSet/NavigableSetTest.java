/*
 * @test
 * @summary java.util.NavigableSet API conformance: lower/floor/ceiling/higher (with out-of-range
 *          nulls), pollFirst/pollLast, descendingSet/descendingIterator, boolean-inclusive range
 *          views, subSet(from>to) IllegalArgumentException, and null-element NPE under natural
 *          ordering.
 * @run main NavigableSetTest
 */

import java.util.Arrays;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

public class NavigableSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
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

    static void testNearest() {
        NavigableSet<Integer> s = new TreeSet<>(Arrays.asList(1, 3, 5));
        check(s.lower(3) == 1, "lower");
        check(s.floor(3) == 3, "floor");
        check(s.ceiling(3) == 3, "ceiling");
        check(s.higher(3) == 5, "higher");
        check(s.lower(1) == null && s.higher(5) == null, "out-of-range nulls");
        expectThrows(NullPointerException.class, () -> s.add(null), "add(null) NPE natural");
    }

    static void testPoll() {
        NavigableSet<Integer> s = new TreeSet<>(Arrays.asList(1, 3, 5));
        check(s.pollFirst() == 1, "pollFirst");
        check(s.pollLast() == 5, "pollLast");
        check(s.size() == 1 && s.first() == 3, "size after polls");

        NavigableSet<Integer> empty = new TreeSet<>();
        check(empty.pollFirst() == null && empty.pollLast() == null, "poll empty null");
    }

    static void testDescending() {
        NavigableSet<Integer> d = new TreeSet<>(Arrays.asList(1, 2, 3));
        check(d.descendingSet().first() == 3 && d.descendingSet().last() == 1, "descendingSet");
        Iterator<Integer> it = d.descendingIterator();
        check(it.next() == 3 && it.next() == 2 && it.next() == 1, "descendingIterator");
    }

    static void testRangeViews() {
        NavigableSet<Integer> r = new TreeSet<>();
        for (int i = 1; i <= 6; i++) r.add(i);
        check(r.subSet(2, true, 4, true).equals(new TreeSet<>(Arrays.asList(2, 3, 4))),
                "subSet inclusive");
        check(r.headSet(3, false).equals(new TreeSet<>(Arrays.asList(1, 2))), "headSet exclusive");
        check(r.tailSet(4, false).equals(new TreeSet<>(Arrays.asList(5, 6))), "tailSet exclusive");
        expectThrows(IllegalArgumentException.class, () -> r.subSet(4, true, 2, true),
                "subSet from>to IAE");
    }

    public static void main(String[] args) {
        testNearest();
        testPoll();
        testDescending();
        testRangeViews();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
