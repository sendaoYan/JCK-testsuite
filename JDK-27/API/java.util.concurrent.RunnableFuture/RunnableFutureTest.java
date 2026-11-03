/*
 * @test
 * @summary java.util.concurrent.RunnableFuture conformance: white-box structure + black-box
 *          behavior (run + Future get/isDone/isCancelled via FutureTask).
 * @run main RunnableFutureTest
 */

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class RunnableFutureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + n + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.RunnableFuture");
        check(c.isInterface(), "RunnableFuture is an interface");
        Class<?>[] ifs = c.getInterfaces();
        boolean runnable = false, future = false;
        for (Class<?> i : ifs) {
            if (i == Runnable.class) runnable = true;
            if (i == Future.class) future = true;
        }
        check(runnable, "extends Runnable");
        check(future, "extends Future");
        hasMethod(c, "run");
        // FutureTask implements it
        Class<?> ft = cls("java.util.concurrent.FutureTask");
        check(RunnableFuture.class.isAssignableFrom(ft), "FutureTask implements RunnableFuture");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RunnableFuture<Integer> f = new FutureTask<>(() -> 42);
        check(f instanceof Runnable, "instance is a Runnable");
        check(!f.isDone() && !f.isCancelled(), "not done/cancelled before run");
        f.run();
        check(f.isDone() && !f.isCancelled(), "done, not cancelled after run");
        check(f.get() == 42, "get returns the result");

        // the Runnable(Runnable, V) form stores the supplied result
        RunnableFuture<String> r = new FutureTask<>(() -> { }, "fixed");
        r.run();
        check("fixed".equals(r.get()), "Runnable with fixed result");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
