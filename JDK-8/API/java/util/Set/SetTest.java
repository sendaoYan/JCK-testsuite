/*
 * @test
 * @summary java.util.Set API conformance: add returns false for duplicates, no duplicate
 *          elements, contains/remove, and addAll (union).
 * @run main SetTest
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBasics() {
        Set<String> s = new HashSet<>();
        check(s.add("a") && s.add("b"), "add returns true");
        check(!s.add("a"), "add duplicate returns false");
        check(s.size() == 2, "size excludes duplicates");
        check(s.contains("a") && !s.contains("z"), "contains");
        check(s.remove("a") && !s.contains("a"), "remove");
        check(s.size() == 1, "size after remove");
    }

    static void testBulk() {
        Set<String> s = new HashSet<>(Arrays.asList("b"));
        Set<String> s2 = new HashSet<>(Arrays.asList("b", "c"));
        s.addAll(s2);
        check(s.equals(new HashSet<>(Arrays.asList("b", "c"))), "addAll (union)");
    }

    public static void main(String[] args) {
        testBasics();
        testBulk();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
