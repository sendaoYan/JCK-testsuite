/*
 * @test
 * @summary java.util.concurrent.ForkJoinPool$ManagedBlocker conformance: white-box structure + black-box behavior.
 * @run main ManagedBlockerTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ManagedBlockerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.ForkJoinPool$ManagedBlocker");
        check(c.isInterface(), "ManagedBlocker is an interface");
        hasMethod(c, "block");
        hasMethod(c, "isReleasable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(ForkJoinPool.ManagedBlocker.class.isInterface(), "is an interface");
        ForkJoinPool.ManagedBlocker releasable = new ForkJoinPool.ManagedBlocker() {
            public boolean block() throws InterruptedException { return false; }
            public boolean isReleasable() { return true; }
        };
        ForkJoinPool.managedBlock(releasable);
        check(true, "managedBlock returns for a releasable blocker");

        final boolean[] blocked = new boolean[1];
        ForkJoinPool.ManagedBlocker needsBlock = new ForkJoinPool.ManagedBlocker() {
            public boolean block() throws InterruptedException { blocked[0] = true; return true; }
            public boolean isReleasable() { return false; }
        };
        ForkJoinPool.managedBlock(needsBlock);
        check(blocked[0], "block() invoked when not releasable");
    }

    // =============== contract additions ===============

    // ---- Part A: full white-box surface ----
    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = cls("java.util.concurrent.ForkJoinPool$ManagedBlocker");
        java.lang.reflect.Method block = c.getMethod("block");
        java.lang.reflect.Method rel = c.getMethod("isReleasable");
        check(block.getReturnType() == boolean.class, "block() returns boolean");
        check(rel.getReturnType() == boolean.class, "isReleasable() returns boolean");
        check(!block.isDefault() && !rel.isDefault(), "no default methods");
        boolean declaresInterrupted = false;
        for (Class<?> ex : block.getExceptionTypes())
            if (ex == InterruptedException.class) declaresInterrupted = true;
        check(declaresInterrupted, "block() declares InterruptedException (only checked exception allowed)");
        check(rel.getExceptionTypes().length == 0, "isReleasable() declares no checked exceptions");
    }

    // ---- Part B: releasable blocker returns without calling block() ----
    static void testReleasable() throws Exception {
        final AtomicInteger blockCalls = new AtomicInteger();
        ForkJoinPool.ManagedBlocker b = new ForkJoinPool.ManagedBlocker() {
            public boolean block() { blockCalls.incrementAndGet(); return true; }
            public boolean isReleasable() { return true; }
        };
        ForkJoinPool.managedBlock(b);
        check(blockCalls.get() == 0, "block() never invoked when already releasable");
    }

    // ---- Part C: blocking blocker invoked exactly once ----
    static void testBlockingBlocker() throws Exception {
        final CountDownLatch open = new CountDownLatch(0); // pre-opened: block returns at once
        final AtomicInteger calls = new AtomicInteger();
        final AtomicBoolean released = new AtomicBoolean(false);
        ForkJoinPool.ManagedBlocker b = new ForkJoinPool.ManagedBlocker() {
            public boolean isReleasable() { return released.get(); }
            public boolean block() throws InterruptedException {
                calls.incrementAndGet();
                boolean r = open.await(3, TimeUnit.SECONDS);
                released.set(true);
                return r;
            }
        };
        ForkJoinPool.managedBlock(b);
        check(calls.get() == 1, "block() invoked exactly once for a non-releasable blocker");
        check(released.get(), "blocker became releasable after block()");

        // null blocker is rejected
        try {
            ForkJoinPool.managedBlock(null);
            check(false, "managedBlock(null) must throw NPE");
        } catch (NullPointerException e) {
            check(true, "managedBlock(null) -> NullPointerException");
        }
    }

    // ---- Part D: exceptions from block() are relayed ----
    static void testExceptionRelay() throws Exception {
        final InterruptedException ie = new InterruptedException("ie-block");
        ForkJoinPool.ManagedBlocker ieB = new ForkJoinPool.ManagedBlocker() {
            public boolean isReleasable() { return false; }
            public boolean block() throws InterruptedException { throw ie; }
        };
        try {
            ForkJoinPool.managedBlock(ieB);
            check(false, "InterruptedException from block() must propagate");
        } catch (InterruptedException e) {
            check(e == ie, "managedBlock relays the ORIGINAL InterruptedException object");
        }

        final RuntimeException re = new IllegalStateException("re-block");
        ForkJoinPool.ManagedBlocker reB = new ForkJoinPool.ManagedBlocker() {
            public boolean isReleasable() { return false; }
            public boolean block() { throw re; }
        };
        try {
            ForkJoinPool.managedBlock(reB);
            check(false, "RuntimeException from block() must propagate");
        } catch (RuntimeException e) {
            check(e == re, "managedBlock relays the ORIGINAL RuntimeException object");
        }
    }

    // ---- Part E: managedBlock inside a pool worker (compensation path) ----
    static void testInsideWorker() throws Exception {
        final CountDownLatch release = new CountDownLatch(1);
        ForkJoinPool pool = new ForkJoinPool(1);
        try {
            ForkJoinTaskBox f = new ForkJoinTaskBox(release);
            ForkJoinTaskBox.submit(pool, f);
            release.countDown();
            check(f.awaitResult(5), "worker-level managedBlock completed with value 99");
        } finally {
            release.countDown();
            pool.shutdownNow();
        }
    }

    /** Small helper so the daemon/no-thread rules stay observable in one place. */
    static class ForkJoinTaskBox {
        final CountDownLatch release;
        final CountDownLatch done = new CountDownLatch(1);
        volatile int value = -1;
        ForkJoinTaskBox(CountDownLatch release) { this.release = release; }
        static void submit(ForkJoinPool pool, final ForkJoinTaskBox box) {
            pool.execute(new java.util.concurrent.RecursiveAction() {
                protected void compute() {
                    try {
                        ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker() {
                            public boolean isReleasable() { return false; }
                            public boolean block() throws InterruptedException { return box.release.await(4, TimeUnit.SECONDS); }
                        });
                    } catch (InterruptedException e) {
                        box.value = -2;
                        box.done.countDown();
                        return;
                    }
                    box.value = 99;
                    box.done.countDown();
                }
            });
        }
        boolean awaitResult(int seconds) throws InterruptedException {
            if (!done.await(seconds, TimeUnit.SECONDS)) return false;
            return value == 99;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxStructure();
        testReleasable();
        testBlockingBlocker();
        testExceptionRelay();
        testInsideWorker();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
