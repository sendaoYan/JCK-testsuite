/*
 * @test
 * @summary java.lang.Thread.UncaughtExceptionHandler conformance: white-box structure + black-box
 *          behavior (direct invocation, per-thread handler, default handler restored in finally).
 * @run main UncaughtExceptionHandlerTest
 */

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class UncaughtExceptionHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = UncaughtExceptionHandler.class;
        check(c.isInterface(), "UncaughtExceptionHandler is an interface");
        Method m = c.getMethod("uncaughtException", Thread.class, Throwable.class);
        check(Modifier.isAbstract(m.getModifiers()), "uncaughtException is abstract");
        check(m.getReturnType() == void.class, "uncaughtException returns void");
        check(UncaughtExceptionHandler.class.isAssignableFrom(ThreadGroup.class),
              "ThreadGroup implements UncaughtExceptionHandler");
    }

    // ---- Part 2: black-box (behavioral) ----
    static final Thread[] seenThread = {null};
    static final Throwable[] seenThrowable = {null};
    static final UncaughtExceptionHandler recorder = (t, e) -> {
        seenThread[0] = t;
        seenThrowable[0] = e;
    };

    static void testDirectInvocation() {
        Thread t = Thread.currentThread();
        Throwable ex = new RuntimeException("direct");
        recorder.uncaughtException(t, ex);
        check(seenThread[0] == t, "uncaughtException receives the thread");
        check(seenThrowable[0] == ex, "uncaughtException receives the throwable");
    }

    static void testPerThreadHandler() throws Exception {
        Thread t = new Thread(() -> { throw new IllegalStateException("uncaught-per-thread"); }, "per-thread");
        t.setUncaughtExceptionHandler(recorder);
        check(t.getUncaughtExceptionHandler() == recorder, "getUncaughtExceptionHandler returns the handler");
        t.start();
        t.join();
        check(seenThread[0] == t, "handler invoked for the dying thread");
        check(seenThrowable[0] instanceof IllegalStateException
              && "uncaught-per-thread".equals(seenThrowable[0].getMessage()),
              "handler received the thread's exception");
    }

    static void testDefaultHandler() throws Exception {
        final Throwable[] defaultSeen = {null};
        Thread.UncaughtExceptionHandler saved = null;
        try {
            saved = Thread.getDefaultUncaughtExceptionHandler();
            Thread.UncaughtExceptionHandler myDefault = (t, e) -> defaultSeen[0] = e;
            Thread.setDefaultUncaughtExceptionHandler(myDefault);
            check(Thread.getDefaultUncaughtExceptionHandler() == myDefault,
                  "setDefault/getDefault round-trip");

            // A per-thread handler takes precedence: with both installed, the dying
            // thread uses its own handler and the default is not consulted.
            final Thread[] seenThread2 = {null};
            Thread t = new Thread(() -> { throw new UnsupportedOperationException("precedence"); },
                                  "precedence-thread");
            t.setUncaughtExceptionHandler((th, ex) -> { seenThread2[0] = th; seenThrowable[0] = ex; });
            t.start();
            t.join();
            check(seenThread2[0] == t, "per-thread handler observed the dying thread");
            check(seenThrowable[0] instanceof UnsupportedOperationException
                  && "precedence".equals(seenThrowable[0].getMessage()),
                  "per-thread handler takes precedence over the default");
            check(defaultSeen[0] == null, "default handler not consulted when overridden");

            // The default handler can also be invoked through its getter (same contract).
            Thread.getDefaultUncaughtExceptionHandler()
                  .uncaughtException(Thread.currentThread(), new RuntimeException("via-getter"));
            check(defaultSeen[0] instanceof RuntimeException
                  && "via-getter".equals(defaultSeen[0].getMessage()),
                  "default handler receives (thread, throwable) via direct call");
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(saved);
        }
        check(Thread.getDefaultUncaughtExceptionHandler() == saved, "default handler restored");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testDirectInvocation();
        testPerThreadHandler();
        testDefaultHandler();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
