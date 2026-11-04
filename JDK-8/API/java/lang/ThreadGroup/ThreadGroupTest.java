/*
 * @test
 * @summary java.lang.ThreadGroup conformance: white-box structure (implements
 *          UncaughtExceptionHandler, two constructors) + black-box behavior
 *          (name, parent linkage, max/setMaxPriority, daemon, activeCount,
 *          enumerate threads/groups, destroy empty vs non-empty ITSE, interrupt,
 *          uncaughtException callable, toString/checkAccess).
 * @run main ThreadGroupTest
 */

import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ThreadGroupTest {
    static int passed;

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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ThreadGroup");
        check(!c.isInterface(), "ThreadGroup is a class");
        check(Thread.UncaughtExceptionHandler.class.isAssignableFrom(c),
              "ThreadGroup implements UncaughtExceptionHandler");
        c.getConstructor(String.class);
        c.getConstructor(ThreadGroup.class, String.class);
        passed += 2;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testConstructors() {
        ThreadGroup current = Thread.currentThread().getThreadGroup();
        ThreadGroup g = new ThreadGroup("jck-tg");
        check("jck-tg".equals(g.getName()), "ThreadGroup(String) name");
        check(g.getParent() == current, "ThreadGroup(String) parent is current group");

        ThreadGroup parent = new ThreadGroup("jck-tg-parent");
        ThreadGroup child = new ThreadGroup(parent, "jck-tg-child");
        check(child.getParent() == parent, "ThreadGroup(parent,name) explicit parent");
        check("jck-tg-child".equals(child.getName()), "ThreadGroup(parent,name) name");

        // JDK-8 does NOT null-check the name (only the parent): a null name is
        // stored verbatim rather than throwing NPE.
        check(new ThreadGroup((String) null).getName() == null, "ThreadGroup((String)null) stores null name");
        expectNPE(() -> new ThreadGroup((ThreadGroup) null, "x"), "ThreadGroup((ThreadGroup)null,name)");
        check(new ThreadGroup(parent, (String) null).getName() == null,
              "ThreadGroup(parent,(String)null) stores null name");
    }

    static void testPriority() {
        ThreadGroup parent = new ThreadGroup("jck-tg-pri-parent");
        parent.setMaxPriority(Thread.MAX_PRIORITY - 1);
        ThreadGroup child = new ThreadGroup(parent, "jck-tg-pri-child");
        check(child.getMaxPriority() == Thread.MAX_PRIORITY - 1, "child inherits parent max priority");
        child.setMaxPriority(Thread.MIN_PRIORITY);
        check(child.getMaxPriority() == Thread.MIN_PRIORITY, "setMaxPriority lowers");
    }

    static void testDaemon() {
        ThreadGroup g = new ThreadGroup("jck-tg-daemon");
        check(!g.isDaemon(), "new group not daemon");
        g.setDaemon(true);
        check(g.isDaemon(), "setDaemon(true)");
    }

    static void testCountsAndEnumerate() throws Exception {
        ThreadGroup g = new ThreadGroup("jck-tg-counts");
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread worker = new Thread(g, () -> {
            started.countDown();
            try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "jck-worker");
        worker.setDaemon(true);
        worker.start();
        try {
            check(started.await(10, TimeUnit.SECONDS), "worker started in group");
            check(g.activeCount() >= 1, "activeCount >= 1 with live worker");
            Thread[] threads = new Thread[g.activeCount() + 1];
            int n = g.enumerate(threads, false);
            check(n >= 1, "enumerate(Thread[],false) returns >= 1");
            boolean foundWorker = false;
            for (int i = 0; i < n; i++) if ("jck-worker".equals(threads[i].getName())) foundWorker = true;
            check(foundWorker, "enumerate lists the worker thread");

            ThreadGroup[] groups = new ThreadGroup[g.activeGroupCount() + 1];
            int ng = g.enumerate(groups, false);
            check(ng >= 0, "enumerate(ThreadGroup[],false) returns >= 0");
        } finally {
            release.countDown();
            try { worker.join(10_000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
    }

    static void testDestroy() {
        ThreadGroup empty = new ThreadGroup("jck-tg-empty");
        empty.destroy();
        check(empty.isDestroyed(), "destroyed empty group");

        ThreadGroup busy = new ThreadGroup("jck-tg-busy");
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread worker = new Thread(busy, () -> {
            started.countDown();
            try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        worker.setDaemon(true);
        worker.start();
        try {
            started.await(10, TimeUnit.SECONDS);
            try {
                busy.destroy();
                throw new AssertionError("destroy non-empty group: expected IllegalThreadStateException");
            } catch (IllegalThreadStateException expected) {
                passed++;
            }
            check(!busy.isDestroyed(), "non-empty group not destroyed");
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            release.countDown();
            try { worker.join(10_000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
    }

    // Overrides uncaughtException to record (and NOT re-propagate to the harness),
    // so forwarding is observable without reaching the root group.
    static class RecordingGroup extends ThreadGroup {
        volatile Thread caughtThread;
        volatile Throwable caught;
        RecordingGroup(String name) { super(name); }
        @Override public void uncaughtException(Thread t, Throwable e) { caughtThread = t; caught = e; }
    }

    static void testMisc() {
        RecordingGroup parent = new RecordingGroup("jck-tg-misc-parent");
        ThreadGroup child = new ThreadGroup(parent, "jck-tg-misc-child");

        check(child.toString().contains("jck-tg-misc-child"), "toString contains name");
        child.checkAccess();
        passed++;

        // Deprecated, but callable in JDK-8: must not throw on a fresh group.
        child.allowThreadSuspension(false);
        passed++;

        // Default uncaughtException forwards to the parent group.
        Thread t = new Thread("jck-ue");
        RuntimeException e = new RuntimeException("jck");
        child.uncaughtException(t, e);
        check(parent.caughtThread == t && parent.caught == e, "uncaughtException forwards to parent");
        passed++;

        // interrupt() on an empty group must not throw.
        child.interrupt();
        passed++;

        // list() must not throw.
        child.list();
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructors();
        testPriority();
        testDaemon();
        testCountsAndEnumerate();
        testDestroy();
        testMisc();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
