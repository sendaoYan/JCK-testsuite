/*
 * @test
 * @summary java.util.LinkedHashSet API conformance: no duplicates, and iteration order
 *          following insertion order.
 * @run main LinkedHashSetTest
 */

import java.util.LinkedHashSet;

public class LinkedHashSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOrderAndDuplicates() {
        LinkedHashSet<String> s = new LinkedHashSet<>();
        check(s.add("a") && s.add("b") && s.add("c"), "add");
        check(!s.add("a"), "duplicate rejected");
        check(s.size() == 3, "size");
        StringBuilder sb = new StringBuilder();
        for (String x : s) {
            sb.append(x);
        }
        check("abc".equals(sb.toString()), "insertion order preserved");
        check(s.remove("b") && !s.contains("b"), "remove");
    }

    public static void main(String[] args) {
        testOrderAndDuplicates();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
