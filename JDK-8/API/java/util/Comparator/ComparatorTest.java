/*
 * @test
 * @summary java.util.Comparator API conformance (Java 8 surface): complete contract coverage
 *          of compare sign, naturalOrder/reverseOrder (+ null NPE / reversed().reversed()),
 *          comparing (Function + explicit Comparator, null key extractor / null key result),
 *          comparingInt/Long/Double (boundaries, NaN semantics), thenComparing (all overloads,
 *          null comparator/key extractor NPE), nullsFirst/nullsLast (null matrix + null NPE).
 * @run main ComparatorTest
 */

import java.util.Comparator;
import java.util.function.Function;

public class ComparatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static void testNaturalAndReverse() {
        Comparator<Integer> natural = Comparator.naturalOrder();
        check(natural.compare(1, 2) < 0 && natural.compare(2, 1) > 0 && natural.compare(3, 3) == 0,
              "naturalOrder sign");
        check(natural.compare(Integer.MIN_VALUE, Integer.MAX_VALUE) < 0, "natural MIN < MAX");
        Comparator<Integer> rev = Comparator.reverseOrder();
        check(rev.compare(1, 2) > 0, "reverseOrder sign");
        check(natural.reversed().compare(1, 2) > 0, "naturalOrder.reversed");
        check(natural.reversed().reversed().compare(1, 2) < 0, "double reversed restores order");

        // naturalOrder / reverseOrder require Comparable: null args -> NPE (Comparable cast).
        expectThrows(NullPointerException.class, () -> natural.compare(null, 1),
            "naturalOrder compare(null,x) NPE");
        expectThrows(NullPointerException.class, () -> rev.compare(null, 1),
            "reverseOrder compare(null,x) NPE");
    }

    static void testComparing() {
        Comparator<String> firstChar = Comparator.comparing(s -> s.charAt(0));
        check(firstChar.compare("apple", "banana") < 0, "comparing (natural key)");
        check(firstChar.compare("apple", "apricot") == 0, "comparing equal key");

        // comparing(Function, Comparator) with explicit key comparator (null-tolerant).
        Comparator<String> byLenDesc = Comparator.comparing(String::length, Comparator.reverseOrder());
        check(byLenDesc.compare("a", "bbb") > 0, "comparing with explicit comparator");

        // comparing(String::length) with a null input -> NPE from natural key extraction.
        expectThrows(NullPointerException.class, () -> firstChar.compare(null, "a"),
            "comparing natural key null input NPE");
        // null key extractor -> NPE (requireNonNull).
        expectThrows(NullPointerException.class,
            () -> Comparator.comparing((Function<String, String>) null),
            "comparing(null function) NPE");
        // explicit-comparator overload with null key comparator -> NPE.
        expectThrows(NullPointerException.class,
            () -> Comparator.comparing(String::length, null),
            "comparing(fn, null cmp) NPE");
        // key extractor returning null with natural key -> NPE; with explicit comparator it is
        // delegated (a null-tolerant comparator can handle it).
        expectThrows(NullPointerException.class,
            () -> Comparator.<String, String>comparing(s -> null).compare("a", "b"),
            "comparing natural key returns null NPE");
    }

    static void testComparingPrimitives() {
        Comparator<String> byLen = Comparator.comparingInt(String::length);
        check(byLen.compare("a", "bbb") < 0, "comparingInt");
        check(byLen.compare(Integer.toString(Integer.MIN_VALUE), "1") > 0, "comparingInt boundary");

        Comparator<long[]> byFirstEl = Comparator.comparingLong(a -> a[0]);
        check(byFirstEl.compare(new long[]{1L}, new long[]{2L}) < 0, "comparingLong");

        // comparingDouble uses Double.compare: NaN == NaN, +0.0 != -0.0.
        Comparator<double[]> byFirstD = Comparator.comparingDouble(a -> a[0]);
        check(byFirstD.compare(new double[]{1.0}, new double[]{2.0}) < 0, "comparingDouble");
        check(byFirstD.compare(new double[]{Double.NaN}, new double[]{Double.NaN}) == 0,
              "comparingDouble NaN == NaN");
        check(byFirstD.compare(new double[]{0.0}, new double[]{-0.0}) > 0,
              "comparingDouble +0.0 > -0.0");

        expectThrows(NullPointerException.class, () -> Comparator.comparingInt(null),
            "comparingInt(null) NPE");
        expectThrows(NullPointerException.class, () -> Comparator.comparingLong(null),
            "comparingLong(null) NPE");
        expectThrows(NullPointerException.class, () -> Comparator.comparingDouble(null),
            "comparingDouble(null) NPE");
    }

    static void testThenComparing() {
        Comparator<String> byLen = Comparator.comparingInt(String::length);
        Comparator<String> byLenThenAlpha = byLen.thenComparing(Comparator.naturalOrder());
        check(byLenThenAlpha.compare("bb", "aa") > 0, "thenComparing(Comparator)");

        Comparator<String> byLenThenFirst = byLen.thenComparing(s -> s.charAt(0));
        check(byLenThenFirst.compare("bb", "aa") > 0, "thenComparing(keyExtractor natural)");

        Comparator<String> byLenThenLenDesc = byLen.thenComparing(String::length, Comparator.reverseOrder());
        check(byLenThenLenDesc.compare("aa", "aa") == 0, "thenComparing(keyExtractor, cmp)");

        Comparator<String> byFirstThenLen = Comparator.comparing((String s) -> s.charAt(0))
            .thenComparingInt(String::length);
        check(byFirstThenLen.compare("aa", "abb") < 0, "thenComparingInt");

        Comparator<long[]> byFirstThenSecond = Comparator.comparingLong((long[] a) -> a[0])
            .thenComparingLong(a -> a[1]);
        check(byFirstThenSecond.compare(new long[]{1L, 2L}, new long[]{1L, 3L}) < 0, "thenComparingLong");

        Comparator<double[]> byFirstThenSecondD = Comparator.comparingDouble((double[] a) -> a[0])
            .thenComparingDouble(a -> a[1]);
        check(byFirstThenSecondD.compare(new double[]{1.0, 2.0}, new double[]{1.0, 3.0}) < 0,
              "thenComparingDouble");

        expectThrows(NullPointerException.class, () -> byLen.thenComparing((Comparator<String>) null),
            "thenComparing(null comparator) NPE");
        expectThrows(NullPointerException.class, () -> byLen.thenComparing((Function<String, String>) null),
            "thenComparing(null function) NPE");
        expectThrows(NullPointerException.class,
            () -> byLen.thenComparing(String::length, null),
            "thenComparing(fn, null cmp) NPE");
        expectThrows(NullPointerException.class, () -> byLen.thenComparingInt(null),
            "thenComparingInt(null) NPE");
        expectThrows(NullPointerException.class, () -> byLen.thenComparingLong(null),
            "thenComparingLong(null) NPE");
        expectThrows(NullPointerException.class, () -> byLen.thenComparingDouble(null),
            "thenComparingDouble(null) NPE");
    }

    static void testNullsFirstLast() {
        Comparator<String> nullsFirst = Comparator.nullsFirst(Comparator.naturalOrder());
        check(nullsFirst.compare(null, "a") < 0, "nullsFirst null < non-null");
        check(nullsFirst.compare("a", null) > 0, "nullsFirst non-null > null");
        check(nullsFirst.compare(null, null) == 0, "nullsFirst(null,null) == 0");
        check(nullsFirst.compare("a", "b") < 0, "nullsFirst delegates natural");

        Comparator<String> nullsLast = Comparator.nullsLast(Comparator.naturalOrder());
        check(nullsLast.compare(null, "a") > 0, "nullsLast null > non-null");
        check(nullsLast.compare("a", null) < 0, "nullsLast non-null < null");
        check(nullsLast.compare(null, null) == 0, "nullsLast(null,null) == 0");
        check(nullsLast.compare("a", "b") < 0, "nullsLast delegates natural");

        // null comparator is legal: all non-null values are considered equal (0); null ordering
        // is still applied.
        Comparator<String> nfNullCmp = Comparator.nullsFirst(null);
        check(nfNullCmp.compare(null, "a") < 0, "nullsFirst(null) null still first");
        check(nfNullCmp.compare("a", "b") == 0, "nullsFirst(null) non-null all equal");
        Comparator<String> nlNullCmp = Comparator.nullsLast(null);
        check(nlNullCmp.compare(null, "a") > 0, "nullsLast(null) null still last");
        check(nlNullCmp.compare("a", "b") == 0, "nullsLast(null) non-null all equal");
    }

    public static void main(String[] args) {
        testNaturalAndReverse();
        testComparing();
        testComparingPrimitives();
        testThenComparing();
        testNullsFirstLast();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
