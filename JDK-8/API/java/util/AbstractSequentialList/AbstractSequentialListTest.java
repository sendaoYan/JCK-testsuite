/*
 * @test
 * @summary java.util.AbstractSequentialList API conformance: inherited list operations via a
 *          LinkedList.
 * @run main AbstractSequentialListTest
 */

import java.util.AbstractSequentialList;
import java.util.LinkedList;

public class AbstractSequentialListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractSequentialList() {
        AbstractSequentialList<Integer> l = new LinkedList<>();
        l.add(1);
        l.add(2);
        check(l.size() == 2, "size");
        check(l.get(0) == 1, "get");
        check(l.remove(0) == 1, "remove");
        check(l.size() == 1, "size after remove");
    }

    public static void main(String[] args) {
        testAbstractSequentialList();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
