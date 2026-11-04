/*
 * @test
 * @summary java.util.concurrent.ForkJoinPool$ManagedBlocker conformance: white-box structure + black-box behavior.
 * @run main ManagedBlockerTest
 */

import java.util.concurrent.*;

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
