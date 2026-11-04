/*
 * @test
 * @summary java.util.concurrent.CopyOnWriteArraySet API conformance: set semantics.
 * @run main CopyOnWriteArraySetTest
 */

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArraySet;

public class CopyOnWriteArraySetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCopyOnWriteArraySet() {
        CopyOnWriteArraySet<String> s = new CopyOnWriteArraySet<String>();
        check(s.add("a"), "add first");
        check(!s.add("a"), "add duplicate returns false");
        check(s.add("b"), "add second");
        check(s.size() == 2, "size");
        check(s.contains("a") && s.contains("b"), "contains");
        check(s.remove("a") && s.size() == 1, "remove");
        check(s.containsAll(Arrays.asList("b")), "containsAll");
    }

    public static void main(String[] args) {
        testCopyOnWriteArraySet();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
