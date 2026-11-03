/*
 * @test
 * @summary java.util.concurrent.Executor conformance: white-box structure + black-box behavior.
 * @run main ExecutorTest
 */

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExecutorTest {
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
        Class<?> c = cls("java.util.concurrent.Executor");
        check(c.isInterface(), "Executor is an interface");
        hasMethod(c, "execute", Runnable.class);
        // single abstract method -> functional interface
        check(c.getDeclaredMethods().length == 1, "exactly one abstract method (functional interface)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // (a) a synchronous Executor implementation runs the task inline
        AtomicBoolean ran = new AtomicBoolean();
        Executor direct = new Executor() {
            public void execute(Runnable r) { r.run(); }
        };
        direct.execute(() -> ran.set(true));
        check(ran.get(), "execute runs the task");

        // (b) a real executor accepts a Runnable via execute() and runs it
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            AtomicBoolean done = new AtomicBoolean();
            Executor e = pool; // ExecutorService IS-A Executor
            e.execute(() -> done.set(true));
            pool.shutdown();
            check(pool.awaitTermination(10, TimeUnit.SECONDS), "executor terminates after shutdown");
            check(done.get(), "task executed by a real executor");
        } finally {
            pool.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
