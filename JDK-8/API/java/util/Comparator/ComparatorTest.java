/*
 * @test
 * @summary java.util.Comparator API conformance: compare sign, naturalOrder/reverseOrder,
 *          and the Java 8 comparing/comparingInt/thenComparing/reversed combinators.
 * @run main ComparatorTest
 */

import java.util.Comparator;

public class ComparatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNaturalAndReverse() {
        Comparator<Integer> natural = Comparator.naturalOrder();
        check(natural.compare(1, 2) < 0 && natural.compare(2, 1) > 0 && natural.compare(3, 3) == 0,
              "naturalOrder sign");
        Comparator<Integer> rev = Comparator.reverseOrder();
        check(rev.compare(1, 2) > 0, "reverseOrder");
    }

    static void testCombinators() {
        Comparator<String> byLen = Comparator.comparingInt(String::length);
        check(byLen.compare("a", "bbb") < 0, "comparingInt");
        check(byLen.reversed().compare("a", "bbb") > 0, "reversed");
        Comparator<String> byLenThenAlpha = byLen.thenComparing(Comparator.naturalOrder());
        check(byLenThenAlpha.compare("bb", "aa") > 0, "thenComparing");
        Comparator<String> firstChar = Comparator.comparing(s -> s.charAt(0));
        check(firstChar.compare("apple", "banana") < 0, "comparing");
    }

    public static void main(String[] args) {
        testNaturalAndReverse();
        testCombinators();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
