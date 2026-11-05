/*
 * @test
 * @summary java.lang.LazyConstant conformance: white-box structure + black-box behavior.
 * @run main LazyConstantTest
 * @enablePreview
 */

import java.lang.LazyConstant;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

public class LazyConstantTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.LazyConstant");
        check(c.isInterface(), "LazyConstant is an interface");
        check(Supplier.class.isAssignableFrom(c), "LazyConstant extends Supplier");
        Method of = c.getMethod("of", Supplier.class);
        check(Modifier.isStatic(of.getModifiers()), "of is static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testOfAndGet() {
        final int[] calls = {0};
        LazyConstant<String> lc = LazyConstant.of(() -> { calls[0]++; return "v"; });
        check(lc != null, "of non-null");
        check(calls[0] == 0, "supplier not called eagerly");
        check("v".equals(lc.get()), "get evaluates");
        check("v".equals(lc.get()), "get cached");
        check(calls[0] == 1, "supplier called exactly once (lazy + cached)");
        check(lc instanceof Supplier, "is Supplier");
    }

    static void testContract() {
        LazyConstant<String> lc = LazyConstant.of(() -> "v");
        check(lc.equals(lc), "equals self");
        check(lc.hashCode() == lc.hashCode(), "hashCode stable");
        check(lc.toString() != null, "toString non-null");
    }

    // ============================================================
    // ---- Part 3: contract deepening (2026-11, appended) ----
    // ============================================================

    static void testOfNullSupplier() {
        try {
            LazyConstant<Object> lc = LazyConstant.of(null);
            if (lc.get() == lc) { } // force use
            throw new AssertionError("of(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            check(true, "of(null) -> NullPointerException eagerly");
        }
    }

    // A failing supplier is evaluated exactly once; the failure is cached and every
    // subsequent get() throws NoSuchElementException naming the original failure.
    static void testFailingSupplierCached() {
        final int[] calls = {0};
        RuntimeException boom = new IllegalStateException("boom");
        LazyConstant<Integer> bad = LazyConstant.of(() -> {
            calls[0]++;
            throw boom;
        });
        try {
            bad.get();
            throw new AssertionError("expected NoSuchElementException");
        } catch (java.util.NoSuchElementException expected) {
            check(expected.getMessage().equals(
                      "Unable to access the constant because java.lang.IllegalStateException"
                      + " was thrown at initial computation"),
                  "first failure message exact");
            check(expected.getCause() == boom, "first failure carries the original cause");
        }
        try {
            bad.get();
            throw new AssertionError("expected NoSuchElementException on second get");
        } catch (java.util.NoSuchElementException expected) {
            check(expected.getMessage().equals(
                      "Unable to access the constant because java.lang.IllegalStateException"
                      + " was thrown at initial computation"),
                  "second get fails with the same message");
            check(expected.getCause() == null, "cause only attached to the first report");
        }
        try {
            bad.get();
        } catch (java.util.NoSuchElementException expected) {
            check(true, "third get still fails (failure memoized)");
        }
        check(calls[0] == 1, "failing supplier still evaluated exactly once");
    }

    // A null value is rejected like a failure.
    static void testNullValueRejected() {
        final int[] calls = {0};
        LazyConstant<Object> nul = LazyConstant.of(() -> { calls[0]++; return null; });
        try {
            nul.get();
            throw new AssertionError("null value: expected NoSuchElementException");
        } catch (java.util.NoSuchElementException expected) {
            check(expected.getMessage().equals(
                      "Unable to access the constant because java.lang.NullPointerException"
                      + " was thrown at initial computation"),
                  "null value failure message exact");
        }
        check(calls[0] == 1, "null-producing supplier evaluated exactly once");
    }

    static void testEqualsSemantics() {
        Supplier<String> s = () -> "v";
        LazyConstant<String> a = LazyConstant.of(s);
        LazyConstant<String> b = LazyConstant.of(s);
        check(a.equals(a), "reflexive");
        check(!a.equals(b), "each of() yields a distinct constant (not value-equal)");
        check(!a.equals(null), "equals(null) false");
        check(!a.equals("v"), "equals(other type) false");
        a.get();
        check(a.equals(a), "self-equality survives evaluation");
        check(a.hashCode() == a.hashCode(), "hashCode stable after evaluation");
    }

    // toString evolves from "computing function=..." to "[value]" (impl-pinned on 27.0.1).
    static void testToStringEvolution() {
        LazyConstant<String> lc = LazyConstant.of(() -> "v");
        String before = lc.toString();
        check(before != null, "toString before get non-null");
        check(!before.contains("[v]"), "value not yet embedded before get");
        String got = lc.get();
        String after = lc.toString();
        check("v".equals(got), "value produced");
        check(after != null && after.contains("[v]"), "toString after get embeds the value");
        check(!after.equals(before), "toString changes when the value is materialized");
    }

    // Supplier view (LazyConstant extends Supplier): same cached value, no re-evaluation.
    static void testSupplierView() {
        final int[] calls = {0};
        LazyConstant<Integer> lc = LazyConstant.of(() -> { calls[0]++; return 7; });
        Supplier<Integer> sup = lc;
        check(Integer.valueOf(7).equals(sup.get()), "Supplier.get evaluates");
        check(Integer.valueOf(7).equals(lc.get()), "LazyConstant.get cached");
        check(Integer.valueOf(7).equals(sup.get()), "Supplier.get cached");
        check(calls[0] == 1, "single evaluation across both views");
    }

    // Identity: the same instance is returned on every get for object values.
    static void testValueIdentity() {
        LazyConstant<Object> lc = LazyConstant.of(() -> new Object());
        Object first = lc.get();
        check(first != null, "object value non-null");
        check(lc.get() == first, "get returns the identical cached instance");
    }

    // Composition: constants may depend on other constants.
    static void testComposition() {
        LazyConstant<Integer> base = LazyConstant.of(() -> 21);
        LazyConstant<Integer> dbl = LazyConstant.of(() -> base.get() * 2);
        check(dbl.get() == 42, "composed constant computes from the base");
        check(dbl.get() == 42 && base.get() == 21, "both constants cached");
    }

    // Two constants from the same supplier evaluate independently.
    static void testIndependentConstants() {
        final int[] calls = {0};
        LazyConstant<String> a = LazyConstant.of(() -> { calls[0]++; return "x"; });
        LazyConstant<String> b = LazyConstant.of(() -> { calls[0]++; return "x"; });
        a.get();
        b.get();
        check(calls[0] == 2, "each constant triggers its own single evaluation");
    }

    // Concurrency: concurrent get() still evaluates the supplier exactly once.
    static void testConcurrentSingleEvaluation() throws Exception {
        final int[] calls = {0};
        LazyConstant<String> shared = LazyConstant.of(() -> {
            calls[0]++;
            return "c";
        });
        Thread[] ts = new Thread[4];
        final String[] results = new String[4];
        java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(4);
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            ts[idx] = new Thread(() -> {
                try {
                    start.await();
                    results[idx] = shared.get();
                } catch (InterruptedException e) {
                    // leave result null
                } finally {
                    done.countDown();
                }
            }, "lcI-lazy-" + idx);
            ts[idx].start();
        }
        start.countDown();
        check(done.await(30, java.util.concurrent.TimeUnit.SECONDS),
              "all getters finished (bounded latch)");
        for (Thread t : ts) t.join(30000);
        check(calls[0] == 1, "supplier evaluated exactly once under concurrency");
        boolean allSame = true;
        for (String r : results) {
            if (!"c".equals(r)) allSame = false;
        }
        check(allSame, "every thread observed the same value");
    }

    static void testPreviewSurface() throws Exception {
        Class<?> c = Class.forName("java.lang.LazyConstant");
        boolean hasOf = false;
        for (Method m : c.getMethods()) {
            if ("of".equals(m.getName())) hasOf = true;
        }
        check(hasOf, "static of(Supplier) present via reflection");
        check(!c.isAnnotationPresent(FunctionalInterface.class),
              "not annotated @FunctionalInterface (still a lambda target)");
    }


    static void testBlackBox() throws Exception {
        testOfAndGet();
        testContract();
        testOfNullSupplier();
        testFailingSupplierCached();
        testNullValueRejected();
        testEqualsSemantics();
        testToStringEvolution();
        testSupplierView();
        testValueIdentity();
        testComposition();
        testIndependentConstants();
        testConcurrentSingleEvaluation();
        testPreviewSurface();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
