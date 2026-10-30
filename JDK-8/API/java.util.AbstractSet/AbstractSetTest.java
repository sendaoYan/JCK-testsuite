/*
 * @test
 * @summary java.util.AbstractSet API conformance: inherited set operations via a HashSet.
 * @run main AbstractSetTest
 */

import java.util.AbstractSet;
import java.util.HashSet;

public class AbstractSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractSet() {
        AbstractSet<Integer> s = new HashSet<>();
        s.add(1);
        s.add(2);
        check(s.size() == 2, "size");
        check(s.contains(1), "contains");
        check(!s.isEmpty(), "not empty");
        check(s.remove(1), "remove");
        check(s.size() == 1, "size after remove");
    }

    public static void main(String[] args) {
        testAbstractSet();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
