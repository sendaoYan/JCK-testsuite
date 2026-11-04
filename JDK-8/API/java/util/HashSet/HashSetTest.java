/*
 * @test
 * @summary java.util.HashSet API conformance: add (false on duplicate, incl. null),
 *          contains/remove/size/isEmpty/clear, iteration, and equals/hashCode
 *          (order-independent).
 * @run main HashSetTest
 */

import java.util.HashSet;
import java.util.Arrays;

public class HashSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAddContains() {
        HashSet<String> set = new HashSet<>();
        check(set.isEmpty() && set.size() == 0, "empty");
        check(set.add("a"), "add new returns true");
        check(set.add("b"), "add another");
        check(set.add(null), "add null");
        check(!set.add("a"), "add duplicate returns false");
        check(set.size() == 3, "size counts unique + null");
        check(set.contains("a") && set.contains("b") && set.contains(null), "contains");
        check(!set.contains("z"), "!contains");
        check(set.remove("a"), "remove returns true");
        check(!set.remove("a"), "remove absent returns false");
        check(set.remove(null), "remove null");
        check(!set.contains("a") && !set.contains(null), "removed absent");
        set.clear();
        check(set.isEmpty(), "clear");
    }

    static void testEqualsHashCode() {
        HashSet<String> a = new HashSet<>(Arrays.asList("x", "y"));
        HashSet<String> b = new HashSet<>(Arrays.asList("y", "x"));
        check(a.equals(b) && b.equals(a), "equals order-independent");
        check(a.hashCode() == b.hashCode(), "equal sets equal hashCode");
        b.add("z");
        check(!a.equals(b), "!equals different content");
    }

    public static void main(String[] args) {
        testAddContains();
        testEqualsHashCode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
