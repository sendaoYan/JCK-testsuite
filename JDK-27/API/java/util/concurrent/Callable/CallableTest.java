/*
 * @test
 * @summary java.util.concurrent.Callable conformance: white-box structure + black-box behavior.
 * @run main CallableTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CallableTest {
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
        Class<?> c = cls("java.util.concurrent.Callable");
        check(c.isInterface(), "Callable is an interface");
        check(c.getDeclaredMethods().length == 1, "single abstract method (functional interface)");
        hasMethod(c, "call");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Callable<Integer> c = new Callable<Integer>() {
            public Integer call() { return 7; }
        };
        check(c.call() == 7, "call returns the computed value");

        // call() may throw checked exceptions
        Callable<String> boom = () -> { throw new java.io.IOException("io"); };
        try {
            boom.call();
            check(false, "expected exception");
        } catch (java.io.IOException expected) {
            check("io".equals(expected.getMessage()), "call() propagates a checked exception");
        }

        // used via an ExecutorService
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> f = exec.submit(() -> 21 * 2);
            check(f.get() == 42, "Callable submitted to executor");
        } finally {
            exec.shutdown();
        }
        check(exec.awaitTermination(10, TimeUnit.SECONDS), "executor terminates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
