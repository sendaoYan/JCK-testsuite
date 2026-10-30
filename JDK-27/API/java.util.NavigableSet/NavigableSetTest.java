/*
 * @test
 * @summary java.util.NavigableSet API conformance: lower/floor/ceiling/higher (with
 *          out-of-range nulls), pollFirst/pollLast, descendingSet and descendingIterator.
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

    static void testNearest() {
        NavigableSet<Integer> s = new TreeSet<>(Arrays.asList(1, 3, 5));
        check(s.lower(3) == 1, "lower");
        check(s.floor(3) == 3, "floor");
        check(s.ceiling(3) == 3, "ceiling");
        check(s.higher(3) == 5, "higher");
        check(s.ceiling(6) == null, "ceiling beyond range is null");
        check(s.lower(1) == null, "lower below range is null");
    }

    static void testPollAndDescending() {
        NavigableSet<Integer> s = new TreeSet<>(Arrays.asList(1, 3, 5));
        check(s.pollFirst() == 1 && s.pollLast() == 5, "pollFirst/pollLast");
        check(s.size() == 1 && s.first() == 3, "size after polls");

        NavigableSet<Integer> desc = new TreeSet<>(Arrays.asList(1, 2, 3)).descendingSet();
        check(desc.first() == 3 && desc.last() == 1, "descendingSet");
        Iterator<Integer> it = new TreeSet<>(Arrays.asList(1, 2, 3)).descendingIterator();
        check(it.next() == 3 && it.next() == 2 && it.next() == 1, "descendingIterator");
    }

    public static void main(String[] args) {
        testNearest();
        testPollAndDescending();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
