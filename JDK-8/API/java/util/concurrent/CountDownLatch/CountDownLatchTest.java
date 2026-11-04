/*
 * @test
 * @summary java.util.concurrent.CountDownLatch API conformance: getCount, countDown to zero,
 *          and await returning immediately once the latch is at zero.
 * @run main CountDownLatchTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CountDownLatchTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCountDown() {
        CountDownLatch latch = new CountDownLatch(2);
        check(latch.getCount() == 2, "initial count");
        latch.countDown();
        check(latch.getCount() == 1, "countDown");
        latch.countDown();
        check(latch.getCount() == 0, "countDown to zero");
    }

    static void testAwait() throws Exception {
        CountDownLatch latch = new CountDownLatch(0);
        check(latch.await(0, TimeUnit.MILLISECONDS), "await on zero latch returns immediately");
        latch.await();
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testCountDown();
        testAwait();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
