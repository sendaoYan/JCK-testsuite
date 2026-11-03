/*
 * @test
 * @summary java.util.Comparator conformance: white-box structure plus black-box behavior of
 *          compare sign, naturalOrder/reverseOrder, comparing/comparingInt/thenComparing/reversed,
 *          nullsFirst/nullsLast, max/min, and thenComparingInt/Long/Double.
 * @run main ComparatorTest
 */

import java.util.Comparator;

public class ComparatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Comparator");
        check(c.isInterface(), "Comparator is an interface");
        c.getMethod("compare", Object.class, Object.class);
        c.getMethod("reversed");
        c.getMethod("thenComparing", Comparator.class);
        c.getMethod("naturalOrder");
        c.getMethod("reverseOrder");
        c.getMethod("nullsFirst", Comparator.class);
        c.getMethod("nullsLast", Comparator.class);
        c.getMethod("comparing", java.util.function.Function.class);
        c.getMethod("comparingInt", java.util.function.ToIntFunction.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testNaturalAndReverse();
        testCombinators();
        testNulls();
        testMinMax();
        testThenComparingVariants();
    }

    static void testNaturalAndReverse() {
        Comparator<Integer> natural = Comparator.naturalOrder();
        check(natural.compare(1, 2) < 0 && natural.compare(2, 1) > 0 && natural.compare(3, 3) == 0,
              "naturalOrder sign");
        Comparator<Integer> rev = Comparator.reverseOrder();
        check(rev.compare(1, 2) > 0, "reverseOrder");
        check(natural.reversed().compare(1, 2) > 0, "naturalOrder.reversed");
    }

    static void testCombinators() {
        Comparator<String> byLen = Comparator.comparingInt(String::length);
        check(byLen.compare("a", "bbb") < 0, "comparingInt");
        check(byLen.reversed().compare("a", "bbb") > 0, "reversed");
        Comparator<String> byLenThenAlpha = byLen.thenComparing(Comparator.naturalOrder());
        check(byLenThenAlpha.compare("bb", "aa") > 0, "thenComparing");
        Comparator<String> firstChar = Comparator.comparing(s -> s.charAt(0));
        check(firstChar.compare("apple", "banana") < 0, "comparing");
        Comparator<String> byLen2 = Comparator.comparing(String::length, Comparator.reverseOrder());
        check(byLen2.compare("a", "bbb") > 0, "comparing with explicit comparator");
    }

    static void testNulls() {
        Comparator<String> nullsFirst = Comparator.nullsFirst(Comparator.naturalOrder());
        check(nullsFirst.compare(null, "a") < 0, "nullsFirst null < non-null");
        check(nullsFirst.compare("a", "b") < 0, "nullsFirst delegates to natural order");

        Comparator<String> nullsLast = Comparator.nullsLast(Comparator.naturalOrder());
        check(nullsLast.compare(null, "a") > 0, "nullsLast null > non-null");
        check(nullsLast.compare("a", "b") < 0, "nullsLast delegates to natural order");
    }

    static void testMinMax() {
        Comparator<Integer> natural = Comparator.naturalOrder();
        check(natural.max(1, 2) == 2, "max");
        check(natural.min(1, 2) == 1, "min");
    }

    static void testThenComparingVariants() {
        Comparator<String> byFirst = Comparator.comparing(s -> s.charAt(0));
        Comparator<String> byLenThenFirst = byFirst.thenComparingInt(String::length);
        check(byLenThenFirst.compare("aa", "abb") < 0, "thenComparingInt");

        Comparator<long[]> byFirstEl = Comparator.comparingLong(a -> a[0]);
        long[] x = {1L}, y = {2L};
        check(byFirstEl.compare(x, y) < 0, "comparingLong");

        Comparator<double[]> byFirstD = Comparator.comparingDouble(a -> a[0]);
        double[] dx = {1.0}, dy = {2.0};
        check(byFirstD.compare(dx, dy) < 0, "comparingDouble");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
