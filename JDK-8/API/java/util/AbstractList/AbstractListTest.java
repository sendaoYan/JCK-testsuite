/*
 * @test
 * @summary java.util.AbstractList API conformance: inherited list operations via an
 *          ArrayList.
 * @run main AbstractListTest
 */

import java.util.AbstractList;
import java.util.ArrayList;

public class AbstractListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractList() {
        AbstractList<Integer> l = new ArrayList<>();
        l.add(10);
        l.add(20);
        check(l.size() == 2, "size");
        check(l.get(0) == 10, "get");
        check(l.set(0, 99) == 10, "set returns old");
        check(l.get(0) == 99, "set new value");
        check(l.indexOf(20) == 1, "indexOf");
        check(l.subList(0, 1).size() == 1, "subList");
    }

    public static void main(String[] args) {
        testAbstractList();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
