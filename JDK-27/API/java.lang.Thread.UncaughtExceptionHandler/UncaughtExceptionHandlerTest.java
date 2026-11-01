/*
 * @test
 * @summary java.lang.Thread.UncaughtExceptionHandler API conformance: uncaughtException.
 * @run main UncaughtExceptionHandlerTest
 */

import java.lang.Thread.UncaughtExceptionHandler;

public class UncaughtExceptionHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUncaughtExceptionHandler() {
        final Object[] seen = new Object[2];
        UncaughtExceptionHandler h = (t, e) -> {
            seen[0] = t;
            seen[1] = e;
        };
        Thread t = Thread.currentThread();
        Throwable ex = new RuntimeException();
        h.uncaughtException(t, ex);
        check(seen[0] == t, "uncaughtException receives the thread");
        check(seen[1] == ex, "uncaughtException receives the throwable");
    }

    public static void main(String[] args) {
        testUncaughtExceptionHandler();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
