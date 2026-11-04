/*
 * @test
 * @summary java.util.AbstractCollection API conformance: inherited bulk operations via an
 *          ArrayList.
 * @run main AbstractCollectionTest
 */

import java.util.AbstractCollection;
import java.util.ArrayList;

public class AbstractCollectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractCollection() {
        AbstractCollection<Integer> c = new ArrayList<>();
        check(c.isEmpty(), "empty");
        c.add(1);
        c.add(2);
        check(c.size() == 2, "size");
        check(c.contains(2), "contains");
        check(!c.isEmpty(), "not empty");
        check(c.toArray().length == 2, "toArray");
    }

    public static void main(String[] args) {
        testAbstractCollection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
