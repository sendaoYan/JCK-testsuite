/*
 * @test
 * @summary java.util.concurrent.ConcurrentSkipListSet API conformance: sorted add/first/last,
 *          duplicate rejection, contains, and pollFirst.
 * @run main ConcurrentSkipListSetTest
 */

import java.util.concurrent.ConcurrentSkipListSet;

public class ConcurrentSkipListSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSet() {
        ConcurrentSkipListSet<Integer> s = new ConcurrentSkipListSet<>();
        check(s.add(3) && s.add(1) && s.add(2), "add");
        check(!s.add(2), "duplicate rejected");
        check(s.size() == 3, "size");
        check(s.first() == 1 && s.last() == 3, "first/last");
        check(s.contains(2) && !s.contains(9), "contains");
        check(s.pollFirst() == 1, "pollFirst");
        check(s.first() == 2, "first after pollFirst");
    }

    public static void main(String[] args) {
        testSet();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
