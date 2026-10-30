/*
 * @test
 * @summary java.util.concurrent.Exchanger API conformance: two threads exchange objects.
 * @run main ExchangerTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.atomic.AtomicReference;

public class ExchangerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testExchanger() throws Exception {
        final Exchanger<String> ex = new Exchanger<>();
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicReference<String> received = new AtomicReference<>();

        Thread t = new Thread(() -> {
            try {
                received.set(ex.exchange("from-worker"));
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            } finally {
                done.countDown();
            }
        });
        t.start();

        String got = ex.exchange("from-main");
        done.await();
        t.join(5000);
        check("from-worker".equals(got), "main received worker object");
        check("from-main".equals(received.get()), "worker received main object");
    }

    public static void main(String[] args) throws Exception {
        testExchanger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
