/*
 * @test
 * @summary java.util.concurrent.ExecutorService conformance: white-box structure + black-box
 *          behavior (submit/get, invokeAll/invokeAny, shutdown/awaitTermination/close).
 * @run main ExecutorServiceTest
 */

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceTest {
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
        Class<?> c = cls("java.util.concurrent.ExecutorService");
        check(c.isInterface(), "ExecutorService is an interface");
        Class<?>[] ifs = c.getInterfaces();
        boolean extendsExecutor = false, extendsAutoCloseable = false;
        for (Class<?> i : ifs) {
            if (i == Executor.class) extendsExecutor = true;
            if (i == AutoCloseable.class) extendsAutoCloseable = true;
        }
        check(extendsExecutor, "extends Executor");
        check(extendsAutoCloseable, "extends AutoCloseable");
        hasMethod(c, "shutdown");
        hasMethod(c, "shutdownNow");
        hasMethod(c, "isShutdown");
        hasMethod(c, "isTerminated");
        hasMethod(c, "awaitTermination", long.class, TimeUnit.class);
        hasMethod(c, "submit", Callable.class);
        hasMethod(c, "invokeAll", java.util.Collection.class);
        hasMethod(c, "invokeAny", java.util.Collection.class);
        hasMethod(c, "close");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ExecutorService exec = Executors.newFixedThreadPool(3);
        try {
            // submit variants
            Future<Integer> c = exec.submit((Callable<Integer>) () -> 42);
            check(c.get() == 42, "submit(Callable).get");
            Future<String> r = exec.submit(() -> { }, "ok");
            check("ok".equals(r.get()), "submit(Runnable, result).get");
            Future<?> v = exec.submit(() -> { });
            check(v.get() == null, "submit(Runnable).get returns null");

            // invokeAll returns in-order, all-completed futures
            List<Callable<Integer>> tasks = Arrays.asList(() -> 1, () -> 2, () -> 3);
            List<Future<Integer>> results = exec.invokeAll(tasks);
            check(results.size() == 3, "invokeAll size");
            for (Future<Integer> f : results) check(f.isDone(), "invokeAll futures are done");
            check(results.get(0).get() == 1 && results.get(1).get() == 2 && results.get(2).get() == 3,
                  "invokeAll preserves order and results");

            // invokeAny returns one of the results
            Integer any = exec.invokeAny(tasks);
            check(any != null && any >= 1 && any <= 3, "invokeAny returns a valid result");
        } finally {
            exec.shutdown();
        }
        check(exec.isShutdown(), "isShutdown after shutdown");
        check(exec.awaitTermination(10, TimeUnit.SECONDS), "awaitTermination returns true");
        check(exec.isTerminated(), "isTerminated after termination");
        check(exec.shutdownNow().isEmpty(), "shutdownNow returns empty list when already terminated");

        // close() (AutoCloseable default) initiates shutdown
        ExecutorService c2 = Executors.newSingleThreadExecutor();
        c2.submit(() -> 7).get();
        c2.close();
        check(c2.isShutdown(), "close() shuts down the service");
        check(c2.awaitTermination(10, TimeUnit.SECONDS), "closed service terminates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
