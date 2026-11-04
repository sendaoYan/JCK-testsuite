/*
 * @test
 * @summary java.util.concurrent.CountedCompleter API conformance: completer/pending-count.
 * @run main CountedCompleterTest
 */

import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CountedCompleterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class CC extends CountedCompleter<Void> {
        CC(CountedCompleter<?> parent) {
            super(parent);
        }
        public void compute() {
        }
    }

    static void testCountedCompleter() {
        CC root = new CC(null);
        CC child = new CC(root);
        check(root.getCompleter() == null, "root has no completer");
        check(child.getCompleter() == root, "child completer is the parent");
        check(child.getPendingCount() == 0, "initial pending count 0");
        child.addToPendingCount(2);
        check(child.getPendingCount() == 2, "addToPendingCount");
        child.tryComplete();
        check(child.getPendingCount() == 1, "tryComplete decrements pending");
        child.tryComplete();
        check(child.getPendingCount() == 0, "tryComplete reaches zero");
    }

    // =============== contract additions ===============

    static class PlainCC extends CountedCompleter<Void> {
        PlainCC(CountedCompleter<?> p) { super(p); }
        PlainCC(CountedCompleter<?> p, int initial) { super(p, initial); }
        PlainCC(int initial) { super(null, initial); }
        public void compute() { }
    }

    static class CountingCC extends CountedCompleter<Void> {
        final AtomicInteger onCompletionCalls;
        CountingCC(CountedCompleter<?> p, AtomicInteger cnt) { super(p); onCompletionCalls = cnt; }
        CountingCC(CountedCompleter<?> p, int initial, AtomicInteger cnt) { super(p, initial); onCompletionCalls = cnt; }
        public void compute() { }
        public void onCompletion(CountedCompleter<?> caller) { onCompletionCalls.incrementAndGet(); }
    }

    // ---- Part A: white-box structure ----
    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.CountedCompleter");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "CountedCompleter is abstract");
        check(c.getSuperclass() == ForkJoinTask.class, "extends ForkJoinTask");
        check(java.lang.reflect.Modifier.isAbstract(c.getDeclaredMethod("compute").getModifiers()),
              "compute() is abstract");
        check(c.getDeclaredConstructor() != null, "no-arg (root) constructor exists");
        check(c.getDeclaredConstructor(CountedCompleter.class) != null, "(completer) constructor exists");
        check(c.getDeclaredConstructor(CountedCompleter.class, int.class) != null,
              "(completer, initialPendingCount) constructor exists");
        String[] names = { "onCompletion", "onExceptionalCompletion", "getCompleter", "getPendingCount",
                "setPendingCount", "addToPendingCount", "compareAndSetPendingCount",
                "decrementPendingCountUnlessZero", "getRoot", "tryComplete", "propagateCompletion",
                "complete", "firstComplete", "nextComplete", "quietlyCompleteRoot", "helpComplete",
                "getRawResult" };
        for (String n : names) {
            boolean found = false;
            for (java.lang.reflect.Method m : c.getMethods())
                if (m.getName().equals(n)) { found = true; break; }
            check(found, "method " + n + " declared");
        }
    }

    // ---- Part B: constructor with initial pending count, decrementPendingCountUnlessZero ----
    static void testPendingCountPrimitives() throws Exception {
        PlainCC three = new PlainCC(3);
        check(three.getCompleter() == null, "no-arg root completer null");
        check(three.getPendingCount() == 3, "initialPendingCount=3 reflected");
        check(three.decrementPendingCountUnlessZero() == 3, "decrement returns the pre-decrement value");
        check(three.getPendingCount() == 2, "pending decremented to 2");
        check(three.decrementPendingCountUnlessZero() == 2, "second decrement returns 2");
        PlainCC one = new PlainCC(1);
        check(one.decrementPendingCountUnlessZero() == 1, "returns 1 at pending=1");
        check(one.getPendingCount() == 0, "pending reaches 0");
        PlainCC zero = new PlainCC(0);
        check(zero.decrementPendingCountUnlessZero() == 0, "returns 0 when already 0");
        check(zero.getPendingCount() == 0, "pending stays 0 (never negative)");
        // compareAndSetPendingCount
        PlainCC cas = new PlainCC(4);
        check(cas.compareAndSetPendingCount(4, 6), "compareAndSetPendingCount succeeds on match");
        check(!cas.compareAndSetPendingCount(4, 8), "compareAndSetPendingCount fails on mismatch");
        check(cas.getPendingCount() == 6, "pending unchanged by failed CAS");
        cas.setPendingCount(0);
        check(cas.getPendingCount() == 0, "setPendingCount to 0");
        cas.addToPendingCount(5);
        check(cas.getPendingCount() == 5, "addToPendingCount adds");
    }

    // ---- Part C: tryComplete state machine (probe-verified: decrement-when-nonzero,
    //      complete-when-already-zero) ----
    static void testTryCompleteStateMachine() throws Exception {
        PlainCC t = new PlainCC(2);
        t.tryComplete();
        check(t.getPendingCount() == 1 && !t.isDone(), "tryComplete at 2 decrements, no completion");
        t.tryComplete();
        check(t.getPendingCount() == 0 && !t.isDone(), "tryComplete at 1 decrements to 0, still no completion");
        t.tryComplete();
        check(t.getPendingCount() == 0 && t.isDone(), "tryComplete at 0 triggers completion");
    }

    // ---- Part D: getRoot across three levels ----
    static void testGetRoot() throws Exception {
        PlainCC root = new PlainCC(0);
        PlainCC mid = new PlainCC(root, 0);
        PlainCC leaf = new PlainCC(mid, 0);
        check(root.getRoot() == root, "root's root is itself");
        check(mid.getRoot() == root, "middle's root");
        check(leaf.getRoot() == root, "leaf's root");
        check(leaf.getCompleter() == mid && mid.getCompleter() == root, "completer chain");
    }

    // ---- Part E: propagateCompletion completes without invoking onCompletion ----
    static void testPropagateCompletion() throws Exception {
        AtomicInteger rootCalls = new AtomicInteger();
        CountingCC root = new CountingCC(null, 1, rootCalls);
        PlainCC a = new PlainCC(root, 0);
        PlainCC b = new PlainCC(root, 0);
        a.propagateCompletion();
        check(root.getPendingCount() == 0 && !root.isDone(),
              "first propagate decrements root pending to 0 without completing");
        b.propagateCompletion();
        check(root.isDone(), "second propagate finds pending 0 and completes root");
        check(rootCalls.get() == 0, "onCompletion never invoked by propagateCompletion");
    }

    // ---- Part F: quietlyCompleteRoot ----
    static void testQuietlyCompleteRoot() throws Exception {
        PlainCC root = new PlainCC(5);
        PlainCC child = new PlainCC(root, 0);
        PlainCC grand = new PlainCC(child, 0);
        grand.quietlyCompleteRoot();
        check(root.isDone(), "root completed regardless of pending count 5");
        check(root.getPendingCount() == 5, "quietlyCompleteRoot does not touch pending count");
        check(!grand.isDone(), "invoking task itself not completed");
    }

    // ---- Part G: firstComplete/nextComplete traversal primitives ----
    static void testTraversalPrimitives() throws Exception {
        PlainCC two = new PlainCC(2);
        check(two.firstComplete() == null, "firstComplete returns null when pending nonzero");
        check(two.getPendingCount() == 1, "firstComplete decremented to 1");
        PlainCC zero = new PlainCC(0);
        check(zero.firstComplete() == zero, "firstComplete returns this when pending is 0");
        PlainCC root = new PlainCC(0);
        PlainCC child = new PlainCC(root, 0);
        Object nxt = child.nextComplete();
        check(nxt == root, "nextComplete returns the completer when its pending is 0");
        check(!root.isDone(), "root not yet complete after traversal visit");
        check(root.nextComplete() == null, "nextComplete on root returns null");
        check(root.isDone(), "nextComplete quietly-completes a rootless task");
    }

    // ---- Part H: complete(T) injection (setRawResult participates) ----
    static class BoxCC extends CountedCompleter<String> {
        String v;
        int computeCalls;
        final AtomicInteger onCompletionCalls;
        BoxCC(CountedCompleter<?> p, AtomicInteger cnt) { super(p); onCompletionCalls = cnt; }
        public String getRawResult() { return v; }
        protected void setRawResult(String s) { v = s; }
        public void compute() { computeCalls++; tryComplete(); }
        public void onCompletion(CountedCompleter<?> c) { onCompletionCalls.incrementAndGet(); }
    }

    static void testCompleteInjection() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        BoxCC box = new BoxCC(null, calls);
        box.complete("X");
        check(box.isDone(), "complete(T) marks the task done");
        check("X".equals(box.getRawResult()), "complete(T) routed through setRawResult");
        check("X".equals(box.invoke()), "invoke after complete(T) returns injected value");
        check(box.computeCalls == 0, "compute never executed");
        check(calls.get() == 1, "onCompletion invoked exactly once by complete(T)");
    }

    // ---- Part I: classic tree completion, sum 1..100 == 5050 (sibling/MapReducer idiom) ----
    static class SumCC extends CountedCompleter<Long> {
        static final int TH = 8;
        final int[] a; final int lo, hi;
        SumCC sibling;
        long result;
        final AtomicInteger onCompletionCalls;
        SumCC(CountedCompleter<?> p, int[] a, int lo, int hi, AtomicInteger cnt) {
            super(p); this.a = a; this.lo = lo; this.hi = hi; this.onCompletionCalls = cnt;
        }
        public Long getRawResult() { return result; }
        public void compute() {
            if (hi - lo <= TH) {
                long s = 0;
                for (int i = lo; i < hi; i++) s += a[i];
                result = s;
            } else {
                int mid = (lo + hi) >>> 1;
                SumCC left = new SumCC(this, a, lo, mid, onCompletionCalls);
                SumCC right = new SumCC(this, a, mid, hi, onCompletionCalls);
                left.sibling = right;
                right.sibling = left;
                setPendingCount(1);  // only right is pending (left runs inline)
                right.fork();
                left.compute();
                return;
            }
            tryComplete();
        }
        public void onCompletion(CountedCompleter<?> caller) {
            onCompletionCalls.incrementAndGet();
            if (caller != this) {
                SumCC child = (SumCC) caller;
                result = child.result + child.sibling.result;
            }
        }
    }

    static int countLeaves(int lo, int hi) {
        if (hi - lo <= SumCC.TH) return 1;
        int mid = (lo + hi) >>> 1;
        return countLeaves(lo, mid) + countLeaves(mid, hi);
    }

    static void testClassicTreeSum() throws Exception {
        int[] data = new int[100];
        for (int i = 0; i < 100; i++) data[i] = i + 1;
        AtomicInteger calls = new AtomicInteger();
        ForkJoinPool pool = new ForkJoinPool(3);
        try {
            SumCC root = new SumCC(null, data, 0, data.length, calls);
            check(pool.invoke(root) == 5050L, "classic tree-completion sum 1..100 == 5050");
            check(root.isDone(), "root completed");
            check(calls.get() == 2 * countLeaves(0, 100) - 1,
                  "onCompletion fired exactly once per node (2*leaves-1 == " + (2 * countLeaves(0, 100) - 1) + ")");
            check(root.join() == 5050L, "join returns the aggregated result");
            check(root.getRawResult() == 5050L, "getRawResult returns aggregated result");
        } finally { pool.shutdownNow(); }
    }

    // ---- Part J: exceptional completion relay ----
    static class FailingCC extends CountedCompleter<Void> {
        final RuntimeException boom;
        final boolean propagate;
        Throwable seenEx;
        CountedCompleter<?> seenCaller;
        int onExcCalls;
        FailingCC(CountedCompleter<?> p, RuntimeException b, boolean propagate) {
            super(p, 0); this.boom = b; this.propagate = propagate;
        }
        public void compute() { throw boom; }
        public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) {
            onExcCalls++;
            seenEx = ex;
            seenCaller = caller;
            return propagate;
        }
    }

    static class RootCC extends CountedCompleter<Void> {
        volatile FailingCC child;
        RootCC() { super(null, 1); }
        public void compute() { child.fork(); }
    }

    static void testExceptionalRelay() throws Exception {
        final RuntimeException boom = new IllegalStateException("cc");
        // propagate == true: completer completes exceptionally with the same exception
        RootCC root = new RootCC();
        FailingCC child = new FailingCC(root, boom, true);
        root.child = child;
        ForkJoinPool pool = new ForkJoinPool(2);
        try {
            try {
                pool.invoke(root);
                check(false, "exceptional relay must surface from invoke");
            } catch (IllegalStateException e) {
                check(true, "invoke rethrows IllegalStateException class");
            }
            check(child.onExcCalls == 1, "onExceptionalCompletion invoked exactly once on the failing task");
            check(child.seenEx == boom, "onExceptionalCompletion saw the original exception object");
            check(child.seenCaller == child, "caller is the failing task itself");
            check(child.isCompletedAbnormally(), "failing child completed abnormally");
            check(root.isCompletedAbnormally(), "root completed abnormally via relay");
            check(root.getException() instanceof IllegalStateException,
                  "root records the exception class (cross-thread may rebuild the object)");
        } finally { pool.shutdownNow(); }

        // propagate == false: completer stays incomplete
        CountedCompleter<Void> noPropRoot = new CountedCompleter<Void>(null, 1) {
            public void compute() { }
        };
        FailingCC noPropChild = new FailingCC(noPropRoot, boom, false);
        noPropChild.fork();
        try {
            noPropChild.join();
            check(false, "failing child join must throw");
        } catch (IllegalStateException e) {
            check(true, "failing child join rethrows ISE");
        }
        check(noPropChild.onExcCalls == 1, "onExceptionalCompletion ran although relay suppressed");
        check(!noPropRoot.isDone(), "completer NOT completed when onExceptionalCompletion returned false");
    }

    public static void main(String[] args) throws Exception {
        testCountedCompleter();
        testWhiteBoxStructure();
        testPendingCountPrimitives();
        testTryCompleteStateMachine();
        testGetRoot();
        testPropagateCompletion();
        testQuietlyCompleteRoot();
        testTraversalPrimitives();
        testCompleteInjection();
        testClassicTreeSum();
        testExceptionalRelay();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
