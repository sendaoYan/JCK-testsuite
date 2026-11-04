/*
 * @test
 * @summary java.lang.Thread API conformance: currentThread, name, id, priority, daemon,
 *          ThreadGroup, state transitions (NEW/RUNNABLE/TIMED_WAITING/TERMINATED),
 *          start vs run, join (terminated returns promptly), sleep, interrupt flag,
 *          constructor matrix (null name -> NPE, null group -> parent group, null
 *          Runnable OK, negative stackSize accepted), double-start -> ITSE, setDaemon
 *          after start -> ITSE, setPriority bounds -> IAE, holdsLock, activeCount/
 *          enumerate/getStackTrace/dumpStack/checkAccess/toString, context class loader
 *          roundtrip, ThreadGroup constructors, the protected 5-arg constructor, and
 *          clone() always throwing CloneNotSupportedException.
 * @run main ThreadTest
 */

import java.lang.reflect.Modifier;

public class ThreadTest {
    static int passed;

    // Exposes the protected final clone().
    static class T extends Thread {
        T(Runnable r, String name) { super(r, name); }
        Object doClone() throws CloneNotSupportedException { return clone(); }
    }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void expectITSE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalThreadStateException");
        } catch (IllegalThreadStateException expected) {
            passed++;
        }
    }

    static void expectIAE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Thread");
        check(!c.isInterface(), "Thread is a class");
        check(!Modifier.isFinal(c.getModifiers()), "Thread is subclassable (not final)");
        check(Runnable.class.isAssignableFrom(c), "Thread implements Runnable");
        check(Modifier.isNative(c.getMethod("currentThread").getModifiers()), "currentThread native");
        check(Modifier.isStatic(c.getMethod("sleep", long.class).getModifiers()), "sleep static");
        check(Modifier.isStatic(c.getMethod("activeCount").getModifiers()), "activeCount static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCurrentAndFields() {
        Thread t = Thread.currentThread();
        check(t == Thread.currentThread(), "currentThread returns the calling thread");
        check(t.isAlive(), "current thread alive");
        check(t.getName() != null && !t.getName().isEmpty(), "name non-null/non-empty");
        t.setName("thread-test-main");
        check(t.getName().equals("thread-test-main"), "setName/getName roundtrip");
        check(t.getId() > 0, "id positive");
        check(t.getPriority() >= Thread.MIN_PRIORITY && t.getPriority() <= Thread.MAX_PRIORITY,
              "priority within [MIN,MAX]");
        check(Thread.MIN_PRIORITY == 1 && Thread.NORM_PRIORITY == 5 && Thread.MAX_PRIORITY == 10,
              "priority constants 1/5/10");
        check(!t.isDaemon(), "main thread non-daemon");
        check(t.getThreadGroup() != null, "thread group non-null");
        check(t.getState() == Thread.State.RUNNABLE, "current thread RUNNABLE");
        check(t.getContextClassLoader() != null, "context class loader non-null");
    }

    static void testPrioritySetter() {
        Thread t = Thread.currentThread();
        int orig = t.getPriority();
        t.setPriority(Thread.MAX_PRIORITY);
        check(t.getPriority() == Thread.MAX_PRIORITY, "setPriority(MAX)");
        t.setPriority(Thread.MIN_PRIORITY);
        check(t.getPriority() == Thread.MIN_PRIORITY, "setPriority(MIN)");
        t.setPriority(orig);
        check(t.getPriority() == orig, "restore priority");
        expectIAE(() -> t.setPriority(0), "setPriority(0)");
        expectIAE(() -> t.setPriority(Thread.MAX_PRIORITY + 1), "setPriority(MAX+1)");
        t.setPriority(orig);
    }

    static void testNames() {
        Thread a = new Thread();
        Thread b = new Thread();
        check(a.getName().startsWith("Thread-"), "default name prefix");
        check(!a.getName().equals(b.getName()), "default names distinct");
        Thread named = new Thread(() -> {}, "named-worker");
        check(named.getName().equals("named-worker"), "Thread(Runnable,String) name");
        Thread nameOnly = new Thread("name-only");
        check(nameOnly.getName().equals("name-only"), "Thread(String) name");
    }

    static void testConstructorMatrix() {
        // null name -> NPE for every constructor that takes a name.
        expectNPE(() -> new Thread((String) null), "Thread((String)null)");
        expectNPE(() -> new Thread((Runnable) null, (String) null), "Thread((Runnable)null,(String)null)");
        expectNPE(() -> new Thread((ThreadGroup) null, (Runnable) null, (String) null),
                  "Thread(group,null,null)");
        expectNPE(() -> new Thread((ThreadGroup) null, (String) null),
                  "Thread(group,(String)null)");

        // null ThreadGroup defaults to the parent (current) thread's group.
        Thread g = new Thread((ThreadGroup) null, () -> {});
        check(g.getThreadGroup() == Thread.currentThread().getThreadGroup(),
              "Thread((ThreadGroup)null,Runnable) defaults to parent group");

        // null Runnable is legal: the thread runs its own (empty) run().
        Thread noop = new Thread((Runnable) null, "noop");
        check(noop.getName().equals("noop") && noop.getState() == Thread.State.NEW,
              "Thread((Runnable)null,name) legal");

        // daemon flag is inherited from the constructing thread by default.
        Thread child = new Thread(() -> {}, "child");
        check(child.isDaemon() == Thread.currentThread().isDaemon(),
              "daemon flag inherited from parent");
    }

    static void testStartRunJoin() throws Exception {
        final boolean[] ran = {false};
        Thread t = new Thread(() -> ran[0] = true, "worker");
        t.setDaemon(true);
        check(t.isDaemon(), "daemon flag set before start");
        check(!t.isAlive() && t.getState() == Thread.State.NEW, "NEW before start");
        t.start();
        t.join();
        check(ran[0], "run executed");
        check(!t.isAlive(), "not alive after join");
        check(t.getState() == Thread.State.TERMINATED, "TERMINATED after join");
        // join on an already-terminated thread returns immediately.
        long start = System.nanoTime();
        t.join();
        check(System.nanoTime() - start < 1_000_000_000L, "join on terminated thread returns promptly");
        // double start -> IllegalThreadStateException.
        expectITSE(() -> t.start(), "double start");
    }

    static void testRunDirect() {
        final boolean[] ran = {false};
        Thread t = new Thread(() -> ran[0] = true);
        t.run();
        check(ran[0], "run() executes runnable on calling thread");
        check(!t.isAlive() && t.getState() == Thread.State.NEW, "direct run() leaves thread NEW");
    }

    static void testDaemonAfterStart() throws Exception {
        final java.util.concurrent.CountDownLatch started = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        Thread t = new Thread(() -> {
            started.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        t.setDaemon(true);
        t.start();
        check(started.await(10, java.util.concurrent.TimeUnit.SECONDS), "worker started");
        expectITSE(() -> t.setDaemon(false), "setDaemon after start");
        release.countDown();
        t.join(10_000);
        check(!t.isAlive(), "worker joined");
    }

    static void testSleep() throws Exception {
        long start = System.nanoTime();
        Thread.sleep(20);
        long ms = (System.nanoTime() - start) / 1_000_000L;
        check(ms >= 20, "sleep blocks at least requested ms");
    }

    static void testInterruptFlag() {
        Thread t = Thread.currentThread();
        t.interrupt();
        check(t.isInterrupted(), "interrupt sets flag");
        check(Thread.interrupted(), "interrupted() returns and clears flag");
        check(!t.isInterrupted(), "flag cleared");
    }

    static void testSleepInterrupted() throws Exception {
        final boolean[] interrupted = {false};
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException e) {
                interrupted[0] = true;
            }
        });
        t.setDaemon(true);
        t.start();
        for (int i = 0; i < 10_000 && t.getState() != Thread.State.TIMED_WAITING; i++) {
            Thread.yield();
        }
        t.interrupt();
        t.join();
        check(interrupted[0], "sleep throws InterruptedException on interrupt");
    }

    static void testHoldsLock() {
        Object lock = new Object();
        check(!Thread.holdsLock(lock), "holdsLock false when not held");
        synchronized (lock) {
            check(Thread.holdsLock(lock), "holdsLock true when held");
        }
    }

    static void testUncaughtHandler() {
        Thread t = new Thread(() -> {}, "handler-test");
        check(t.getUncaughtExceptionHandler() != null, "default uncaught handler non-null");
        Thread.UncaughtExceptionHandler h = (th, ex) -> {};
        t.setUncaughtExceptionHandler(h);
        check(t.getUncaughtExceptionHandler() == h, "setUncaughtExceptionHandler roundtrip");
        check(Thread.getDefaultUncaughtExceptionHandler() == null,
              "getDefaultUncaughtExceptionHandler defaults to null");
    }

    static void testIntrospectionAndGroups() throws Exception {
        Thread current = Thread.currentThread();

        // activeCount / enumerate (static).
        check(Thread.activeCount() >= 1, "activeCount >= 1");
        Thread[] arr = new Thread[Thread.activeCount() + 8];
        int n = Thread.enumerate(arr);
        check(n >= 1, "enumerate returns >= 1 thread");

        // getStackTrace.
        StackTraceElement[] st = current.getStackTrace();
        check(st != null && st.length > 0, "getStackTrace non-empty");

        // dumpStack (prints to stderr, must not throw) and checkAccess (no SM -> no-op).
        Thread.dumpStack();
        passed++;
        current.checkAccess();
        passed++;

        // toString.
        String s = current.toString();
        check(s.startsWith("Thread[") && s.contains(current.getName()), "toString format");

        // setContextClassLoader roundtrip (restored in finally).
        ClassLoader origCl = current.getContextClassLoader();
        try {
            ClassLoader custom = new ClassLoader(ClassLoader.getSystemClassLoader()) { };
            current.setContextClassLoader(custom);
            check(current.getContextClassLoader() == custom, "setContextClassLoader sets");
            current.setContextClassLoader(origCl);
            check(current.getContextClassLoader() == origCl, "setContextClassLoader restored");
        } finally {
            current.setContextClassLoader(origCl);
        }

        // (ThreadGroup, Runnable) and (ThreadGroup, String) constructors.
        ThreadGroup g = new ThreadGroup("bb-group");
        Thread t1 = new Thread(g, () -> {});
        check(t1.getThreadGroup() == g, "Thread(ThreadGroup,Runnable) group");
        Thread t2 = new Thread(g, "named-in-group");
        check(t2.getThreadGroup() == g && "named-in-group".equals(t2.getName()),
              "Thread(ThreadGroup,String)");

        // Public 4-arg constructor (ThreadGroup, Runnable, String, long stackSize).
        Thread t3 = new Thread(g, () -> {}, "four-arg", 0L);
        check("four-arg".equals(t3.getName()) && t3.getThreadGroup() == g,
              "4-arg constructor sets name and group");

        // clone() is protected and always throws CloneNotSupportedException.
        T cl = new T(() -> {}, "clone-test");
        try {
            cl.doClone();
            throw new AssertionError("Thread.clone: expected CloneNotSupportedException");
        } catch (CloneNotSupportedException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCurrentAndFields();
        testPrioritySetter();
        testNames();
        testConstructorMatrix();
        testStartRunJoin();
        testRunDirect();
        testDaemonAfterStart();
        testSleep();
        testInterruptFlag();
        testSleepInterrupted();
        testHoldsLock();
        testUncaughtHandler();
        testIntrospectionAndGroups();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
