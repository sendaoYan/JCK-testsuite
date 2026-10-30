/*
 * @test
 * @summary java.util.concurrent.Semaphore API conformance: availablePermits, acquire,
 *          tryAcquire success/failure, and release.
 * @run main SemaphoreTest
 */

import java.util.concurrent.Semaphore;

public class SemaphoreTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSemaphore() throws Exception {
        Semaphore sem = new Semaphore(2);
        check(sem.availablePermits() == 2, "initial permits");
        sem.acquire();
        check(sem.availablePermits() == 1, "acquire decrements");
        check(sem.tryAcquire(), "tryAcquire succeeds");
        check(sem.availablePermits() == 0, "permits exhausted");
        check(!sem.tryAcquire(), "tryAcquire fails when empty");
        sem.release();
        check(sem.availablePermits() == 1, "release increments");
    }

    public static void main(String[] args) throws Exception {
        testSemaphore();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
