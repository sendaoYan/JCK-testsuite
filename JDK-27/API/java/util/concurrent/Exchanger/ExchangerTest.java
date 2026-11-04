/*
 * @test
 * @summary java.util.concurrent.Exchanger conformance: white-box structure + black-box
 *          behavior (two-thread exchange, timed exchange timeout).
 * @run main ExchangerTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class ExchangerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + n + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.Exchanger");
        check(!c.isInterface(), "Exchanger is a class");
        hasMethod(c, "exchange", Object.class);
        hasMethod(c, "exchange", Object.class, long.class, TimeUnit.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
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
        check(done.await(10, TimeUnit.SECONDS), "worker completed exchange");
        t.join(5000);
        check(!t.isAlive(), "worker thread finished");
        check("from-worker".equals(got), "main received worker object");
        check("from-main".equals(received.get()), "worker received main object");

        // timed exchange without a partner times out
        Exchanger<String> solo = new Exchanger<>();
        try {
            solo.exchange("lonely", 50, TimeUnit.MILLISECONDS);
            check(false, "unpaired timed exchange should time out");
        } catch (TimeoutException expected) {
            check(true, "timed exchange throws TimeoutException when unpaired");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
