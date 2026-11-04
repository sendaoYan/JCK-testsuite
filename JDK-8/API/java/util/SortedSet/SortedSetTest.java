/*
 * @test
 * @summary java.util.SortedSet API conformance: comparator (natural null vs custom), first/last
 *          with empty NoSuchElementException, headSet/tailSet/subSet range views (exclusive/
 *          inclusive), subSet(from>to) IllegalArgumentException, null-element NPE under natural
 *          ordering, and backed view liveness.
 * @run main SortedSetTest
 */

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

public class SortedSetTest {
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

    static void testNaturalOrder() {
        SortedSet<Integer> s = new TreeSet<>(Arrays.asList(3, 1, 2));
        check(s.comparator() == null, "natural ordering comparator is null");
        check(s.first() == 1 && s.last() == 3, "first/last");
        check(s.size() == 3, "size");
        expectThrows(NullPointerException.class, () -> s.add(null), "add(null) NPE natural");
    }

    static void testEmpty() {
        SortedSet<Integer> s = new TreeSet<>();
        expectThrows(NoSuchElementException.class, s::first, "first empty NSEE");
        expectThrows(NoSuchElementException.class, s::last, "last empty NSEE");
    }

    static void testCustomComparator() {
        SortedSet<Integer> s = new TreeSet<>(Comparator.<Integer>reverseOrder());
        s.add(1);
        s.add(2);
        check(s.comparator() != null, "custom comparator is non-null");
        check(s.first() == 2, "reverse comparator first");
    }

    static void testRangeViews() {
        SortedSet<Integer> s = new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5));
        check(s.headSet(3).equals(new TreeSet<>(Arrays.asList(1, 2))), "headSet exclusive");
        check(s.tailSet(3).equals(new TreeSet<>(Arrays.asList(3, 4, 5))), "tailSet inclusive");
        check(s.subSet(2, 4).equals(new TreeSet<>(Arrays.asList(2, 3))), "subSet [from,to)");
        expectThrows(IllegalArgumentException.class, () -> s.subSet(4, 2), "subSet from>to IAE");
    }

    static void testBackedViews() {
        SortedSet<Integer> s = new TreeSet<>(Arrays.asList(1, 2));
        SortedSet<Integer> sub = s.subSet(1, 4);
        check(sub.add(3), "subSet add in range returns true");
        check(s.contains(3), "subSet is live (add reflects)");
        sub.remove(2);
        check(!s.contains(2), "subSet remove reflects in backing set");
    }

    public static void main(String[] args) {
        testNaturalOrder();
        testEmpty();
        testCustomComparator();
        testRangeViews();
        testBackedViews();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
