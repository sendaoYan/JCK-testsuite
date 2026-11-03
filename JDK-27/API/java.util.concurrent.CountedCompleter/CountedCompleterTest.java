/*
 * @test
 * @summary java.util.concurrent.CountedCompleter conformance: white-box structure + black-box behavior.
 * @run main CountedCompleterTest
 */

import java.util.concurrent.*;

public class CountedCompleterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.CountedCompleter");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(ForkJoinTask.class.isAssignableFrom(c), "extends ForkJoinTask");
        hasMethod(c, "compute");
        hasMethod(c, "getCompleter");
        hasMethod(c, "getPendingCount");
        hasMethod(c, "setPendingCount", int.class);
        hasMethod(c, "addToPendingCount", int.class);
        hasMethod(c, "compareAndSetPendingCount", int.class, int.class);
        hasMethod(c, "getRoot");
        hasMethod(c, "tryComplete");
        hasMethod(c, "firstComplete");
        hasMethod(c, "nextComplete");
        hasMethod(c, "complete", Object.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CC root = new CC(null);
        CC child = new CC(root);
        check(root.getCompleter() == null, "root has no completer");
        check(child.getCompleter() == root, "child completer is parent");
        check(child.getRoot() == root, "getRoot returns root");
        check(child.getPendingCount() == 0, "initial pending count 0");
        child.setPendingCount(3);
        check(child.getPendingCount() == 3, "setPendingCount");
        check(child.compareAndSetPendingCount(3, 5), "compareAndSetPendingCount succeeds");
        check(!child.compareAndSetPendingCount(99, 1), "compareAndSetPendingCount fails on mismatch");
        check(child.getPendingCount() == 5, "pending unchanged after failed CAS");
        child.addToPendingCount(2);
        check(child.getPendingCount() == 7, "addToPendingCount");
        child.tryComplete();
        check(child.getPendingCount() == 6, "tryComplete decrements pending");
        CC solo = new CC(null);
        solo.complete(null);
        check(solo.isDone(), "complete() marks task done");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static class CC extends CountedCompleter<Void> {
        CC(CountedCompleter<?> parent) { super(parent); }
        public void compute() {}
    }}
