/*
 * @test
 * @summary java.util.concurrent.ForkJoinPool.ManagedBlocker conformance: managedBlock.
 * @run main ManagedBlockerTest
 */

import java.util.concurrent.ForkJoinPool;

public class ManagedBlockerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testManagedBlocker() throws InterruptedException {
        check(ForkJoinPool.ManagedBlocker.class.isInterface(), "is an interface");
        ForkJoinPool.ManagedBlocker releasable = new ForkJoinPool.ManagedBlocker() {
            public boolean block() throws InterruptedException { return false; }
            public boolean isReleasable() { return true; }
        };
        ForkJoinPool.managedBlock(releasable);
        check(true, "managedBlock returns for a releasable blocker");
    }

    public static void main(String[] args) throws InterruptedException {
        testManagedBlocker();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
