/*
 * @test
 * @summary java.util.TreeSet API conformance: natural/comparator ordering, add (false on
 *          duplicate), contains/remove, first/last, NavigableSet ops (lower/floor/ceiling/
 *          higher, pollFirst/pollLast), and headSet/subSet/tailSet range views.
 * @run main TreeSetTest
 */

import java.util.TreeSet;
import java.util.Comparator;

public class TreeSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNaturalOrder() {
        TreeSet<String> set = new TreeSet<>();
        check(set.comparator() == null, "natural comparator null");
        check(set.add("c") && set.add("a") && set.add("b"), "add returns true");
        check(!set.add("a"), "add duplicate returns false");
        check(set.first().equals("a") && set.last().equals("c"), "first/last");
        check(set.size() == 3 && set.contains("b"), "size/contains");
        check(set.remove("b") && !set.contains("b"), "remove");
    }

    static void testComparator() {
        TreeSet<Integer> set = new TreeSet<>(Comparator.reverseOrder());
        set.add(1);
        set.add(3);
        set.add(2);
        check(set.first() == 3 && set.last() == 1, "reverse comparator order");
    }

    static void testNavigable() {
        TreeSet<Integer> set = new TreeSet<>();
        for (int i = 1; i <= 10; i += 2) {
            set.add(i); // 1,3,5,7,9
        }
        check(set.lower(5) == 3, "lower");
        check(set.floor(5) == 5 && set.floor(4) == 3, "floor");
        check(set.ceiling(5) == 5 && set.ceiling(4) == 5, "ceiling");
        check(set.higher(5) == 7, "higher");
        check(set.pollFirst() == 1 && set.pollLast() == 9, "pollFirst/pollLast");
        check(set.size() == 3, "size after polls");
    }

    static void testRangeViews() {
        TreeSet<Integer> set = new TreeSet<>();
        for (int i = 1; i <= 10; i++) {
            set.add(i);
        }
        check(set.subSet(3, 7).size() == 4 && set.subSet(3, 7).first() == 3
              && set.subSet(3, 7).last() == 6, "subSet exclusive toElement");
        check(set.headSet(4).size() == 3, "headSet");
        check(set.tailSet(8).size() == 3, "tailSet");
        check(set.subSet(3, true, 7, true).size() == 5, "subSet inclusive bounds");
    }

    public static void main(String[] args) {
        testNaturalOrder();
        testComparator();
        testNavigable();
        testRangeViews();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
