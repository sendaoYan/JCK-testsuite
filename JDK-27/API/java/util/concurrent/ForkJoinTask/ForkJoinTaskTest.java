/*
 * @test
 * @summary java.util.concurrent.ForkJoinTask conformance: white-box structure + black-box
 *          behavior (invoke/fork/join/adapt/complete/completeExceptionally/getException).
 * @run main ForkJoinTaskTest
 */

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ForkJoinTaskTest {
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
        Class<?> c = cls("java.util.concurrent.ForkJoinTask");
        check(c.isInterface() == false && java.lang.reflect.Modifier.isAbstract(c.getModifiers()),
              "ForkJoinTask is an abstract class");
        check(Future.class.isAssignableFrom(c), "implements Future");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        hasMethod(c, "fork");
        hasMethod(c, "join");
        hasMethod(c, "invoke");
        hasMethod(c, "cancel", boolean.class);
        hasMethod(c, "get");
        hasMethod(c, "complete", Object.class);
        hasMethod(c, "completeExceptionally", Throwable.class);
        hasMethod(c, "getException");
        hasMethod(c, "isCompletedAbnormally");
        hasMethod(c, "adapt", Callable.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static class Task extends ForkJoinTask<Integer> {
        int result;
        public Integer getRawResult() { return result; }
        protected void setRawResult(Integer value) { result = value; }
        protected boolean exec() { setRawResult(42); return true; }
    }

    static class FibTask extends ForkJoinTask<Integer> {
        final int n;
        int result;
        FibTask(int n) { this.n = n; }
        public Integer getRawResult() { return result; }
        protected void setRawResult(Integer v) { result = v; }
        protected boolean exec() {
            if (n <= 1) { result = n; return true; }
            FibTask a = new FibTask(n - 1);
            FibTask b = new FibTask(n - 2);
            a.fork();
            b.fork();
            result = a.join() + b.join();
            return true;
        }
    }

    static void testBlackBox() throws Exception {
        ForkJoinPool pool = new ForkJoinPool();
        try {
            // invoke computes the raw result
            Task t = new Task();
            check(pool.invoke(t) == 42, "invoke returns the raw result");
            check(t.isDone() && !t.isCancelled(), "task done after invoke");
            check(t.get() == 42, "get after invoke");

            // fork/join recursion
            FibTask fib = new FibTask(10);
            check(pool.invoke(fib) == 55, "fork/join fibonacci(10) == 55");

            // adapt from Runnable/result and Callable
            ForkJoinTask<String> ar = ForkJoinTask.adapt(() -> { }, "done");
            check("done".equals(pool.invoke(ar)), "adapt(Runnable, result)");
            ForkJoinTask<Integer> ac = ForkJoinTask.adapt((Callable<Integer>) () -> 100);
            check(pool.invoke(ac) == 100, "adapt(Callable)");

            // complete(V) and completeExceptionally(Throwable)
            Task c = new Task();
            c.complete(7);
            check(c.isDone() && c.get() == 7, "complete(V)");
            Task e = new Task();
            e.completeExceptionally(new IllegalStateException("x"));
            check(e.isCompletedAbnormally(), "isCompletedAbnormally after completeExceptionally");
            check(e.getException() instanceof IllegalStateException, "getException returns the cause");
        } finally {
            pool.shutdown();
        }
        check(pool.awaitTermination(10, TimeUnit.SECONDS), "pool terminates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
