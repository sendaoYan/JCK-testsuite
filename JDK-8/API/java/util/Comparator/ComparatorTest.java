/*
 * @test
 * @summary java.util.Comparator API conformance (Java 8 surface): complete contract coverage
 *          of compare sign, naturalOrder/reverseOrder (+ null NPE / reversed().reversed()),
 *          comparing (Function + explicit Comparator, null key extractor / null key result),
 *          comparingInt/Long/Double (boundaries, NaN semantics), thenComparing (all overloads,
 *          null comparator/key extractor NPE), nullsFirst/nullsLast (null matrix + null NPE).
 * @run main ComparatorTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
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

    // ---- deepened contract coverage, round 2 (empirically locked on jdk8u372) --

    static void testSgnContract() {
        Comparator<Integer> natural = Comparator.naturalOrder();
        Comparator<Integer> reversed = Comparator.reverseOrder();
        Integer[] vals = {-2, 0, 2};
        for (Comparator<Integer> c : new Comparator[]{natural, reversed, natural.reversed()}) {
            for (Integer a : vals) for (Integer b : vals)
                check(Integer.signum(c.compare(a, b)) == -Integer.signum(c.compare(b, a)),
                      "sgn antisymmetry c(" + a + "," + b + ")");
            check(c.compare(0, 0) == 0, "sgn(x,x) == 0");
        }
        // transitivity of the >-relation induced by the comparator
        for (Comparator<Integer> c : new Comparator[]{natural, reversed}) {
            for (Integer a : vals) for (Integer b : vals) for (Integer d : vals)
                check(!(c.compare(a, b) > 0 && c.compare(b, d) > 0) || c.compare(a, d) > 0,
                      "sgn transitivity c(" + a + "," + b + "," + d + ")");
        }
        // naturalOrder and reverseOrder are exact inversions of each other
        for (Integer a : vals) for (Integer b : vals)
            check(Integer.signum(natural.compare(a, b)) == -Integer.signum(reversed.compare(a, b)),
                  "natural/reverse inversion c(" + a + "," + b + ")");
    }

    static void testThenComparingChain() {
        // 3-level chain: first char, then length, then natural order
        Comparator<String> c3 = Comparator.comparing((String s) -> s.charAt(0))
            .thenComparingInt(String::length)
            .thenComparing(Comparator.naturalOrder());
        List<String> list = new ArrayList<>(Arrays.asList("b2", "a2", "c1", "a1"));
        list.sort(c3);
        check(list.equals(Arrays.asList("a1", "a2", "b2", "c1")),
              "3-level chain sorts [b2,a2,c1,a1] -> [a1,a2,b2,c1]");
        check(c3.compare("b", "a") > 0, "chain primary level dominates");
        check(c3.compare("ax", "ay") < 0, "chain third level breaks full tie");
        check(c3.compare("a1", "a2") < 0 && c3.compare("a2", "a1") > 0, "chain last level antisymmetric");
        // comparing().reversed() flips the sign of the whole chain
        check(c3.compare("a", "b") < 0, "chain orders a before b");
        Comparator<String> c3r = c3.reversed();
        check(c3r.compare("a", "b") > 0, "chain.reversed() sign flip");
        check(c3.reversed().reversed().compare("a", "b") < 0, "chain double reversed restores");
        // reversed() does not mutate the original comparator
        Comparator<Integer> nat = Comparator.naturalOrder();
        Comparator<Integer> rev2 = nat.reversed();
        check(nat.compare(1, 2) < 0 && rev2.compare(1, 2) > 0,
              "reversed() leaves original comparator unchanged");
    }

    static class Pair {
        final int k;
        final String tag;
        Pair(int k, String tag) { this.k = k; this.tag = tag; }
    }

    static void testSortStabilityUnderComparator() {
        // List.sort(TimSort) is stable: equal keys keep encounter order
        List<Pair> ps = new ArrayList<>(Arrays.asList(
            new Pair(2, "a"), new Pair(1, "a"), new Pair(2, "b"),
            new Pair(1, "b"), new Pair(2, "c")));
        ps.sort(Comparator.comparingInt(p -> p.k));
        check(ps.get(0).k == 1 && ps.get(0).tag.equals("a") && ps.get(1).tag.equals("b"),
              "stable sort keeps 1a before 1b");
        check(ps.get(2).tag.equals("a") && ps.get(3).tag.equals("b") && ps.get(4).tag.equals("c"),
              "stable sort keeps 2a,2b,2c encounter order");
        // keys equal under comparator -> comparator itself returns 0
        check(Comparator.comparingInt((Pair p) -> p.k).compare(new Pair(1, "x"), new Pair(1, "y")) == 0,
              "comparator returns 0 for equal keys regardless of identity");
    }

    static void testNullsCompositions() {
        Comparator<String> nfRev = Comparator.nullsFirst(Comparator.reverseOrder());
        check(nfRev.compare("a", "b") > 0, "nullsFirst delegates to reverseOrder");
        check(nfRev.compare(null, "z") < 0, "nullsFirst(reverse) null still first");
        TreeSet<String> ts = new TreeSet<>(nfRev);
        ts.add("b"); ts.add("a"); ts.add(null);
        check(ts.toString().equals("[null, b, a]"),
              "TreeSet(nullsFirst(reverseOrder)) iterates [null, b, a]");
        check(ts.size() == 3 && ts.contains(null) && ts.contains("a"),
              "TreeSet null-tolerant membership");
        check(ts.first() == null, "TreeSet nullsFirst first() == null");
        check("a".equals(ts.last()), "TreeSet reverse natural last() == smallest");
        // reversed() of a null-wrapped comparator flips the null ordering too
        Comparator<String> nfNat = Comparator.nullsFirst(Comparator.<String>naturalOrder());
        check(nfNat.reversed().compare(null, "a") > 0, "nullsFirst(natural).reversed() null moves last");
        check(nfNat.reversed().compare("a", "b") > 0, "nullsFirst(natural).reversed() delegates reversed");
        // nullsLast over an all-equal comparator, then reversed: non-nulls stay equal, null first
        Comparator<String> nlEq = Comparator.nullsLast((String x, String y) -> 0);
        check(nlEq.reversed().compare(null, "a") < 0, "nullsLast(equals).reversed() null first");
        check(nlEq.reversed().compare("a", "b") == 0, "nullsLast(equals).reversed() non-null still equal");
        // nesting null wrappers
        Comparator<String> nested = Comparator.nullsFirst(Comparator.nullsLast(Comparator.naturalOrder()));
        check(nested.compare(null, "a") < 0, "nested nullsFirst(nullsLast) null first");
        check(nested.compare("a", "b") < 0, "nested null wrappers delegate innermost");
    }

    static void testExceptionPropagation() {
        // exception thrown by the key extractor propagates as the SAME instance
        IllegalStateException boom = new IllegalStateException("boom");
        Comparator<String> throwingKey = Comparator.comparing((String s) -> { throw boom; });
        try {
            throwingKey.compare("a", "b");
            throw new AssertionError("expected ISE from key extractor");
        } catch (IllegalStateException e) {
            check(e == boom, "comparing propagates the same exception instance");
        }
        // throwing secondary comparator is only reached on a primary tie
        Comparator<String> withThrowingSecondary =
            Comparator.comparingInt(String::length)
                      .thenComparing((String x, String y) -> { throw boom; });
        try {
            withThrowingSecondary.compare("ab", "cd");  // equal lengths -> tie -> secondary
            throw new AssertionError("expected ISE from secondary comparator");
        } catch (IllegalStateException e) {
            check(e == boom, "thenComparing propagates secondary comparator exception");
        }
        check(withThrowingSecondary.compare("ab", "zzz") < 0,
              "secondary comparator skipped when primary decides");
        // thenComparing key extractor returning null with natural key -> NPE
        expectThrows(NullPointerException.class,
            () -> Comparator.comparing((String s) -> s.charAt(0))
                            .thenComparing((String s) -> (String) null)
                            .compare("ax", "ay"),
            "thenComparing keyExtractor returns null NPE");
        // both-null short-circuits to 0 without invoking the delegate
        Comparator<String> nfThrow =
            Comparator.nullsFirst((String x, String y) -> { throw boom; });
        check(nfThrow.compare(null, null) == 0,
              "nullsFirst(null,null) == 0 without invoking delegate");
    }

    static void testEqualsContract() {
        Comparator<String> c = Comparator.comparing(String::length);
        check(c.equals(c), "comparator equals itself");
        check(!c.equals(null), "comparator.equals(null) false");
        // combinators use identity equality: distinct instances are not equal
        check(!c.equals(Comparator.comparing(String::length)),
              "comparing(fn) instances use identity equals");
        check(!Comparator.nullsFirst(Comparator.naturalOrder())
                        .equals(Comparator.nullsFirst(Comparator.naturalOrder())),
              "nullsFirst(cmp) instances use identity equals");
        // naturalOrder/reverseOrder are cached singletons
        check(Comparator.naturalOrder().equals(Comparator.naturalOrder()),
              "naturalOrder instances are equal");
        check(Comparator.reverseOrder().equals(Comparator.reverseOrder()),
              "reverseOrder instances are equal");
        check(Comparator.naturalOrder().reversed().equals(Comparator.reverseOrder()),
              "naturalOrder().reversed() equals reverseOrder()");
        check(Comparator.reverseOrder().reversed().equals(Comparator.naturalOrder()),
              "reverseOrder().reversed() equals naturalOrder()");
        // equals across unrelated comparator types is false
        check(!Comparator.<String>naturalOrder().equals(Comparator.comparing(String::length)),
              "naturalOrder not equal to comparing instance");
    }

    static void testPrimitiveSpecializationBoundaries() {
        Comparator<long[]> byLong = Comparator.comparingLong(a -> a[0]);
        check(byLong.compare(new long[]{Long.MIN_VALUE}, new long[]{Long.MAX_VALUE}) < 0,
              "comparingLong MIN < MAX");
        check(byLong.compare(new long[]{Long.MAX_VALUE}, new long[]{Long.MIN_VALUE}) > 0,
              "comparingLong MAX > MIN");
        check(byLong.compare(new long[]{-1L}, new long[]{-1L}) == 0, "comparingLong equal == 0");
        Comparator<int[]> byInt = Comparator.comparingInt(a -> a[0]);
        check(byInt.compare(new int[]{Integer.MIN_VALUE}, new int[]{Integer.MAX_VALUE}) < 0,
              "comparingInt MIN < MAX");
        check(byInt.compare(new int[]{7}, new int[]{7}) == 0, "comparingInt equal == 0");
        // comparing(fn).reversed() sign flip and restoration
        check(Comparator.comparing(String::length).reversed().compare("a", "bbb") > 0,
              "comparing(fn).reversed() sign flip");
        check(Comparator.comparing(String::length).reversed().reversed().compare("a", "bbb") < 0,
              "comparing(fn).reversed().reversed() restores order");
        // comparingDouble total order: NaN greatest
        Comparator<double[]> byD = Comparator.comparingDouble(a -> a[0]);
        check(byD.compare(new double[]{Double.NaN}, new double[]{Double.POSITIVE_INFINITY}) > 0,
              "comparingDouble NaN > +Inf");
        check(byD.compare(new double[]{-0.0}, new double[]{0.0}) < 0,
              "comparingDouble -0.0 < +0.0");
    }

    public static void main(String[] args) {
        testNaturalAndReverse();
        testComparing();
        testComparingPrimitives();
        testThenComparing();
        testNullsFirstLast();
        testSgnContract();
        testThenComparingChain();
        testSortStabilityUnderComparator();
        testNullsCompositions();
        testExceptionPropagation();
        testEqualsContract();
        testPrimitiveSpecializationBoundaries();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
