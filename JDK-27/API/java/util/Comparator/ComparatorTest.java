/*
 * @test
 * @summary java.util.Comparator conformance: white-box structure plus black-box behavior of
 *          compare sign, naturalOrder/reverseOrder, comparing/comparingInt/thenComparing/reversed,
 *          nullsFirst/nullsLast, max/min, and thenComparingInt/Long/Double.
 * @run main ComparatorTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

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
        testBoundaries();
        testNullAndExceptionMatrix();
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

    static void testNullAndExceptionMatrix() {
        Comparator<Integer> natural = Comparator.naturalOrder();
        expectThrows(NullPointerException.class, () -> natural.compare(null, 1),
                "naturalOrder compare(null,x) NPE");
        expectThrows(NullPointerException.class,
                () -> Comparator.comparing((java.util.function.Function<String, String>) null),
                "comparing(null function) NPE");
        expectThrows(NullPointerException.class,
                () -> Comparator.comparing(String::length, null), "comparing(fn, null cmp) NPE");
        expectThrows(NullPointerException.class,
                () -> Comparator.<String, String>comparing(s -> null).compare("a", "b"),
                "comparing natural key returns null NPE");
        // null comparator is legal: all non-null values are considered equal (0).
        check(Comparator.nullsFirst(null).compare(null, "a") < 0, "nullsFirst(null) null first");
        check(Comparator.nullsFirst(null).compare("a", "b") == 0, "nullsFirst(null) non-null equal");
        check(Comparator.nullsLast(null).compare(null, "a") > 0, "nullsLast(null) null last");
        check(Comparator.nullsLast(null).compare("a", "b") == 0, "nullsLast(null) non-null equal");
        expectThrows(NullPointerException.class,
                () -> Comparator.comparingInt(String::length).thenComparing((Comparator<String>) null),
                "thenComparing(null comparator) NPE");
        expectThrows(NullPointerException.class,
                () -> Comparator.comparingInt(String::length).thenComparing((java.util.function.Function<String, String>) null),
                "thenComparing(null function) NPE");

        // comparingDouble uses Double.compare: NaN == NaN, +0.0 != -0.0.
        Comparator<double[]> byFirstD = Comparator.comparingDouble(a -> a[0]);
        check(byFirstD.compare(new double[]{Double.NaN}, new double[]{Double.NaN}) == 0,
              "comparingDouble NaN == NaN");
        check(byFirstD.compare(new double[]{0.0}, new double[]{-0.0}) > 0,
              "comparingDouble +0.0 > -0.0");
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

    static void testBoundaries() {
        Comparator<Integer> natural = Comparator.naturalOrder();
        check(natural.compare(Integer.MIN_VALUE, Integer.MAX_VALUE) < 0, "natural MIN < MAX");
        check(natural.compare(Integer.MAX_VALUE, Integer.MIN_VALUE) > 0, "natural MAX > MIN");
        check(natural.compare(Integer.MIN_VALUE, Integer.MIN_VALUE) == 0, "natural MIN == MIN");
        check(natural.reversed().compare(Integer.MIN_VALUE, Integer.MAX_VALUE) > 0, "reversed MIN > MAX");
        Comparator<Integer> byInt = Comparator.comparingInt(x -> x);
        check(byInt.compare(Integer.MIN_VALUE, Integer.MAX_VALUE) < 0, "comparingInt MIN < MAX");
        check(byInt.compare(-1, 1) < 0 && byInt.compare(1, -1) > 0, "comparingInt sign boundary");
        check(byInt.reversed().compare(1, 2) > 0, "reversed of comparingInt");
        Comparator<String> nullsFirst = Comparator.nullsFirst(Comparator.naturalOrder());
        check(nullsFirst.compare(null, null) == 0, "nullsFirst(null, null) == 0");
        Comparator<String> nullsLast = Comparator.nullsLast(Comparator.naturalOrder());
        check(nullsLast.compare(null, null) == 0, "nullsLast(null, null) == 0");
        check(nullsFirst.compare(null, "a") < 0 && nullsLast.compare(null, "a") > 0, "nulls ordering");
        Comparator<String> byLen = Comparator.comparingInt(String::length);
        check(byLen.thenComparing(Comparator.naturalOrder()).compare("a", "b") < 0,
              "thenComparing equal key falls to secondary");
        check(byLen.reversed().reversed().compare("a", "bbb") < 0, "double reversed restores order");
    }

    // ---- deepened contract coverage, round 2 (empirically locked on jdk-27.0.1) --

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
        for (Comparator<Integer> c : new Comparator[]{natural, reversed}) {
            for (Integer a : vals) for (Integer b : vals) for (Integer d : vals)
                check(!(c.compare(a, b) > 0 && c.compare(b, d) > 0) || c.compare(a, d) > 0,
                      "sgn transitivity c(" + a + "," + b + "," + d + ")");
        }
        for (Integer a : vals) for (Integer b : vals)
            check(Integer.signum(natural.compare(a, b)) == -Integer.signum(reversed.compare(a, b)),
                  "natural/reverse inversion c(" + a + "," + b + ")");
    }

    static void testThenComparingChain() {
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
        check(c3.compare("a", "b") < 0, "chain orders a before b");
        check(c3.reversed().compare("a", "b") > 0, "chain.reversed() sign flip");
        check(c3.reversed().reversed().compare("a", "b") < 0, "chain double reversed restores");
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
        List<Pair> ps = new ArrayList<>(Arrays.asList(
            new Pair(2, "a"), new Pair(1, "a"), new Pair(2, "b"),
            new Pair(1, "b"), new Pair(2, "c")));
        ps.sort(Comparator.comparingInt(p -> p.k));
        check(ps.get(0).k == 1 && ps.get(0).tag.equals("a") && ps.get(1).tag.equals("b"),
              "stable sort keeps 1a before 1b");
        check(ps.get(2).tag.equals("a") && ps.get(3).tag.equals("b") && ps.get(4).tag.equals("c"),
              "stable sort keeps 2a,2b,2c encounter order");
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
        Comparator<String> nfNat = Comparator.nullsFirst(Comparator.<String>naturalOrder());
        check(nfNat.reversed().compare(null, "a") > 0, "nullsFirst(natural).reversed() null moves last");
        check(nfNat.reversed().compare("a", "b") > 0, "nullsFirst(natural).reversed() delegates reversed");
        Comparator<String> nlEq = Comparator.nullsLast((String x, String y) -> 0);
        check(nlEq.reversed().compare(null, "a") < 0, "nullsLast(equals).reversed() null first");
        check(nlEq.reversed().compare("a", "b") == 0, "nullsLast(equals).reversed() non-null still equal");
        Comparator<String> nested = Comparator.nullsFirst(Comparator.nullsLast(Comparator.naturalOrder()));
        check(nested.compare(null, "a") < 0, "nested nullsFirst(nullsLast) null first");
        check(nested.compare("a", "b") < 0, "nested null wrappers delegate innermost");
    }

    static void testExceptionPropagation() {
        IllegalStateException boom = new IllegalStateException("boom");
        Comparator<String> throwingKey = Comparator.comparing((String s) -> { throw boom; });
        try {
            throwingKey.compare("a", "b");
            throw new AssertionError("expected ISE from key extractor");
        } catch (IllegalStateException e) {
            check(e == boom, "comparing propagates the same exception instance");
        }
        Comparator<String> withThrowingSecondary =
            Comparator.comparingInt(String::length)
                      .thenComparing((String x, String y) -> { throw boom; });
        try {
            withThrowingSecondary.compare("ab", "cd");
            throw new AssertionError("expected ISE from secondary comparator");
        } catch (IllegalStateException e) {
            check(e == boom, "thenComparing propagates secondary comparator exception");
        }
        check(withThrowingSecondary.compare("ab", "zzz") < 0,
              "secondary comparator skipped when primary decides");
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
        check(!c.equals(Comparator.comparing(String::length)),
              "comparing(fn) instances use identity equals");
        check(!Comparator.nullsFirst(Comparator.naturalOrder())
                        .equals(Comparator.nullsFirst(Comparator.naturalOrder())),
              "nullsFirst(cmp) instances use identity equals");
        check(Comparator.naturalOrder().equals(Comparator.naturalOrder()),
              "naturalOrder instances are equal");
        check(Comparator.reverseOrder().equals(Comparator.reverseOrder()),
              "reverseOrder instances are equal");
        check(Comparator.naturalOrder().reversed().equals(Comparator.reverseOrder()),
              "naturalOrder().reversed() equals reverseOrder()");
        check(Comparator.reverseOrder().reversed().equals(Comparator.naturalOrder()),
              "reverseOrder().reversed() equals naturalOrder()");
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
        check(Comparator.comparing(String::length).reversed().compare("a", "bbb") > 0,
              "comparing(fn).reversed() sign flip");
        check(Comparator.comparing(String::length).reversed().reversed().compare("a", "bbb") < 0,
              "comparing(fn).reversed().reversed() restores order");
        Comparator<double[]> byD = Comparator.comparingDouble(a -> a[0]);
        check(byD.compare(new double[]{Double.NaN}, new double[]{Double.POSITIVE_INFINITY}) > 0,
              "comparingDouble NaN > +Inf");
        check(byD.compare(new double[]{-0.0}, new double[]{0.0}) < 0,
              "comparingDouble -0.0 < +0.0");
    }

    // JDK 21+ default methods min/max (empirically locked)
    static void testMinMaxContract() {
        Comparator<Integer> natural = Comparator.naturalOrder();
        // equal arguments: the FIRST argument is returned (identity, not just equals)
        String x1 = new String("x");
        String x2 = new String("x");
        Comparator<String> natStr = Comparator.naturalOrder();
        check(natStr.max(x1, x2) == x1, "max equal args returns first argument (identity)");
        check(natStr.min(x1, x2) == x1, "min equal args returns first argument (identity)");
        check(natStr.max(x2, x1) == x2, "max equal args reversed order returns first argument");
        check(natStr.min(x2, x1) == x2, "min equal args reversed order returns first argument");
        // reversed comparator: min picks the larger, max the smaller
        Comparator<Integer> rev = Comparator.reverseOrder();
        check(rev.min(1, 2) == 2, "reverseOrder min returns larger");
        check(rev.max(1, 2) == 1, "reverseOrder max returns smaller");
        // comparator that considers everything equal -> both return first arg
        Comparator<String> allEq = (a, b) -> 0;
        String p = new String("p");
        String q = new String("p");
        check(allEq.max(p, q) == p && allEq.min(q, p) == q,
              "all-equal comparator min/max return first argument");
        // null args are passed through to compare -> NPE
        expectThrows(NullPointerException.class, () -> natStr.max(null, "a"),
                "max(null, a) NPE");
        expectThrows(NullPointerException.class, () -> natStr.min("a", null),
                "min(a, null) NPE");
        // chained comparators compose with min/max
        Comparator<String> byLen = Comparator.comparingInt(String::length);
        check(byLen.max("a", "bbb").equals("bbb"), "max by length");
        check(byLen.min("a", "bbb").equals("a"), "min by length");
        check(byLen.max("aa", "bb") == "aa", "max equal keys returns first argument");
    }

    // JDK 8 missing on this side: thenComparing primitive-specialization null checks
    static void testThenComparingPrimitiveNulls() {
        Comparator<String> byLen = Comparator.comparingInt(String::length);
        expectThrows(NullPointerException.class, () -> byLen.thenComparingInt(null),
                "thenComparingInt(null) NPE");
        expectThrows(NullPointerException.class, () -> byLen.thenComparingLong(null),
                "thenComparingLong(null) NPE");
        expectThrows(NullPointerException.class, () -> byLen.thenComparingDouble(null),
                "thenComparingDouble(null) NPE");
        expectThrows(NullPointerException.class,
                () -> byLen.thenComparing(String::length, null),
                "thenComparing(fn, null cmp) NPE");
        expectThrows(NullPointerException.class, () -> Comparator.comparingInt(null),
                "comparingInt(null) NPE");
        expectThrows(NullPointerException.class, () -> Comparator.comparingLong(null),
                "comparingLong(null) NPE");
        expectThrows(NullPointerException.class, () -> Comparator.comparingDouble(null),
                "comparingDouble(null) NPE");
    }

    static void testDefaultMethodReflection() throws Exception {
        Class<?> c = Class.forName("java.util.Comparator");
        c.getMethod("min", Object.class, Object.class);
        c.getMethod("max", Object.class, Object.class);
        c.getMethod("thenComparing", java.util.function.Function.class, Comparator.class);
        c.getMethod("thenComparingInt", java.util.function.ToIntFunction.class);
        c.getMethod("thenComparingLong", java.util.function.ToLongFunction.class);
        c.getMethod("thenComparingDouble", java.util.function.ToDoubleFunction.class);
        c.getMethod("comparingLong", java.util.function.ToLongFunction.class);
        c.getMethod("comparingDouble", java.util.function.ToDoubleFunction.class);
        check(!java.lang.reflect.Modifier.isAbstract(
                  c.getMethod("min", Object.class, Object.class).getModifiers()),
              "min/max are default (non-abstract) interface methods");
        check(!java.lang.reflect.Modifier.isAbstract(
                  c.getMethod("reversed").getModifiers()),
              "reversed is a default interface method");
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSgnContract();
        testThenComparingChain();
        testSortStabilityUnderComparator();
        testNullsCompositions();
        testExceptionPropagation();
        testEqualsContract();
        testPrimitiveSpecializationBoundaries();
        testMinMaxContract();
        testThenComparingPrimitiveNulls();
        testDefaultMethodReflection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
