/*
 * @test
 * @summary java.lang.Thread conformance: white-box structure + black-box behavior.
 * @run main ThreadTest
 */

import java.lang.reflect.Modifier;

public class ThreadTest {
    static int passed;

    // Exposes the protected 5-arg constructor and the protected final clone().
    static class T extends Thread {
        T(ThreadGroup g, Runnable r, String name, long stackSize, boolean inherit) {
            super(g, r, name, stackSize, inherit);
        }
        Object doClone() throws CloneNotSupportedException { return clone(); }
    }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Thread");
        check(!c.isInterface(), "Thread is a class");
        check(!Modifier.isFinal(c.getModifiers()), "Thread is subclassable (not final)");
        check(Runnable.class.isAssignableFrom(c), "Thread implements Runnable");
        check(Modifier.isNative(c.getMethod("currentThread").getModifiers()), "currentThread native");
        check(Modifier.isStatic(c.getMethod("sleep", long.class).getModifiers()), "sleep static");
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
        try {
            t.setPriority(Thread.MAX_PRIORITY + 1);
            throw new AssertionError("setPriority(MAX+1): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
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
    }

    static void testRunDirect() {
        final boolean[] ran = {false};
        Thread t = new Thread(() -> ran[0] = true);
        t.run();
        check(ran[0], "run() executes runnable on calling thread");
        check(!t.isAlive() && t.getState() == Thread.State.NEW, "direct run() leaves thread NEW");
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

    // ---- Part 2b: black-box for the previously uncovered Thread methods ----
    static void testBlackBox() throws Exception {
        Thread current = Thread.currentThread();

        // activeCount / enumerate (static)
        check(Thread.activeCount() >= 1, "activeCount >= 1");
        Thread[] arr = new Thread[Thread.activeCount() + 8];
        int n = Thread.enumerate(arr);
        check(n >= 1, "enumerate returns >= 1 thread");

        // getStackTrace
        StackTraceElement[] st = current.getStackTrace();
        check(st != null && st.length > 0, "getStackTrace non-empty");

        // dumpStack (prints to stderr, must not throw) and checkAccess (no SM -> no-op)
        Thread.dumpStack();
        passed++;
        current.checkAccess();
        passed++;

        // threadId / toString
        check(current.threadId() > 0, "threadId positive");
        String s = current.toString();
        check(s.startsWith("Thread[") && s.contains(current.getName()), "toString format");

        // setContextClassLoader roundtrip (restored in finally)
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

        // (ThreadGroup, Runnable) and (ThreadGroup, String) constructors
        ThreadGroup g = new ThreadGroup("bb-group");
        Thread t1 = new Thread(g, () -> {});
        check(t1.getThreadGroup() == g, "Thread(ThreadGroup,Runnable) group");
        Thread t2 = new Thread(g, "named-in-group");
        check(t2.getThreadGroup() == g && "named-in-group".equals(t2.getName()),
              "Thread(ThreadGroup,String)");

        // Protected 5-arg constructor via subclass.
        T t3 = new T(g, () -> {}, "five-arg", 0L, true);
        check("five-arg".equals(t3.getName()) && t3.getThreadGroup() == g,
              "5-arg constructor sets name and group");

        // clone() is protected final and always throws CloneNotSupportedException.
        try {
            t3.doClone();
            throw new AssertionError("Thread.clone: expected CloneNotSupportedException");
        } catch (CloneNotSupportedException expected) {
            passed++;
        }

        // startVirtualThread: returns a started virtual thread.
        final boolean[] ran = {false};
        Thread vt = Thread.startVirtualThread(() -> ran[0] = true);
        vt.join();
        check(ran[0], "virtual thread ran");
        check(vt.isVirtual(), "isVirtual true");
        check(vt.getState() == Thread.State.TERMINATED, "virtual thread terminated");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCurrentAndFields();
        testPrioritySetter();
        testNames();
        testStartRunJoin();
        testRunDirect();
        testSleep();
        testInterruptFlag();
        testSleepInterrupted();
        testHoldsLock();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
