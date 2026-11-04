/*
 * @test
 * @summary java.util.SortedSet API conformance: first/last, comparator null for natural
 *          ordering, and headSet/tailSet/subSet range views.
 * @run main SortedSetTest
 */

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

public class SortedSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBasics() {
        SortedSet<Integer> s = new TreeSet<>(Arrays.asList(3, 1, 2));
        check(s.first() == 1 && s.last() == 3, "first/last");
        check(s.comparator() == null, "natural ordering comparator is null");
        check(s.size() == 3, "size");
    }

    static void testRangeViews() {
        SortedSet<Integer> s = new TreeSet<>(Arrays.asList(1, 2, 3));
        check(s.headSet(2).equals(new TreeSet<>(Arrays.asList(1))), "headSet (exclusive)");
        check(s.tailSet(2).equals(new TreeSet<>(Arrays.asList(2, 3))), "tailSet (inclusive)");
        check(s.subSet(1, 3).equals(new TreeSet<>(Arrays.asList(1, 2))), "subSet [from, to)");
    }

    public static void main(String[] args) {
        testBasics();
        testRangeViews();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
