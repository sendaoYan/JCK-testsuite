/*
 * @test
 * @summary java.util.concurrent.Exchanger conformance: white-box structure + black-box
 *          behavior (two-thread exchange, timed exchange timeout, timeout does not
 *          poison the exchanger, interrupt, null exchange item, third thread queues).
 * @run main ExchangerTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
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

    // ---- Part 3: contract deepening (appended) ----

    // a TimeoutException does not poison the exchanger: the very same instance
    // still completes a later exchange
    static void testTimeoutDoesNotPoison() throws Exception {
        final Exchanger<String> ex = new Exchanger<>();
        try {
            ex.exchange("first", 50, TimeUnit.MILLISECONDS);
            check(false, "unpaired timed exchange should time out");
        } catch (TimeoutException expected) {
            check(true, "unpaired timed exchange times out");
        }
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicReference<String> workerGot = new AtomicReference<>("unset");
        Thread t = new Thread(() -> {
            entered.countDown();
            try {
                workerGot.set(ex.exchange("worker"));
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            } finally {
                done.countDown();
            }
        });
        t.setDaemon(true);
        t.start();
        check(entered.await(5, TimeUnit.SECONDS), "worker started");
        String got = ex.exchange("main", 5, TimeUnit.SECONDS);
        check(done.await(5, TimeUnit.SECONDS), "worker finished the exchange");
        t.join(5000);
        check("worker".equals(got), "main received the worker value after a prior timeout");
        check("main".equals(workerGot.get()), "worker received the main value after a prior timeout");
    }

    // timeout <= 0 is treated as an already expired timeout
    static void testZeroTimeout() throws Exception {
        try {
            new Exchanger<String>().exchange("x", 0, TimeUnit.MILLISECONDS);
            check(false, "exchange with zero timeout should time out");
        } catch (TimeoutException expected) {
            check(true, "exchange(x, 0, unit) times out immediately when unpaired");
        }
    }

    // interrupting a waiting thread: exchange throws InterruptedException promptly
    static void testInterrupt() throws Exception {
        final Exchanger<String> ex = new Exchanger<>();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean interrupted = new AtomicBoolean();
        Thread t = new Thread(() -> {
            entered.countDown();
            try {
                ex.exchange("x");
            } catch (InterruptedException e) {
                interrupted.set(true);
            } finally {
                done.countDown();
            }
        });
        t.setDaemon(true);
        t.start();
        check(entered.await(5, TimeUnit.SECONDS), "worker started waiting");
        t.interrupt();
        check(done.await(5, TimeUnit.SECONDS), "interrupted exchange returns promptly (bounded)");
        t.join(5000);
        check(!t.isAlive(), "interrupted worker finished");
        check(interrupted.get(), "exchange throws InterruptedException when interrupted");
    }

    // null is a legal item to exchange: the partner receives exactly null
    static void testNullExchange() throws Exception {
        final Exchanger<String> ex = new Exchanger<>();
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicReference<String> workerGot = new AtomicReference<>("unset");
        Thread t = new Thread(() -> {
            try {
                workerGot.set(ex.exchange((String) null));
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            } finally {
                done.countDown();
            }
        });
        t.setDaemon(true);
        t.start();
        String mainGot = ex.exchange("MAIN", 5, TimeUnit.SECONDS);
        check(done.await(5, TimeUnit.SECONDS), "null-exchange worker finished");
        t.join(5000);
        check(mainGot == null, "null is a legal exchange item: partner received null");
        check("MAIN".equals(workerGot.get()), "worker received the non-null value");
    }

    // three concurrent parties but exchanges complete in pairs: a third arrival
    // either joins one of the swaps or queues behind them; whichever thread is
    // left over times out cleanly and nothing throws unexpectedly
    static void testThirdThreadQueues() throws Exception {
        final Exchanger<String> ex = new Exchanger<>();
        final CountDownLatch entered = new CountDownLatch(2);
        final CountDownLatch done = new CountDownLatch(2);
        final AtomicReference<String> aGot = new AtomicReference<>("unset");
        final AtomicReference<String> bGot = new AtomicReference<>("unset");
        final AtomicBoolean aTimedOut = new AtomicBoolean();
        final AtomicBoolean bTimedOut = new AtomicBoolean();
        Thread a = new Thread(() -> {
            entered.countDown();
            try {
                aGot.set(ex.exchange("a", 6, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                aTimedOut.set(true);
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            } finally {
                done.countDown();
            }
        });
        Thread b = new Thread(() -> {
            entered.countDown();
            try {
                bGot.set(ex.exchange("b", 6, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                bTimedOut.set(true);
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            } finally {
                done.countDown();
            }
        });
        a.setDaemon(true);
        b.setDaemon(true);
        a.start();
        b.start();
        check(entered.await(5, TimeUnit.SECONDS), "both queued waiters started");
        String mainGot = null;
        boolean mainTimedOut = false;
        try {
            mainGot = ex.exchange("m1", 2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            mainTimedOut = true;
        }
        check(done.await(10, TimeUnit.SECONDS), "both queued waiters finished (bounded)");
        a.join(5000);
        b.join(5000);
        check(!a.isAlive() && !b.isAlive(), "both queued threads finished");
        if (mainTimedOut) {
            // the two waiters paired with each other; main queued behind them
            check("b".equals(aGot.get()) && "a".equals(bGot.get()),
                  "when main queues behind a swap, the two waiters exchange with each other");
            check(!aTimedOut.get() && !bTimedOut.get(), "the paired waiters did not time out");
        } else {
            // main joined one of the swaps; the leftover waiter queued and timed out
            check("a".equals(mainGot) || "b".equals(mainGot), "main received one of the queued values");
            boolean mainWithA = "a".equals(mainGot) && "m1".equals(aGot.get())
                    && bTimedOut.get() && "unset".equals(bGot.get());
            boolean mainWithB = "b".equals(mainGot) && "m1".equals(bGot.get())
                    && aTimedOut.get() && "unset".equals(aGot.get());
            check(mainWithA || mainWithB,
                  "main paired with exactly one waiter; the other queued and timed out cleanly");
        }
    }

    public static void main(String[] args) throws Exception {
        testExchanger();
        testWhiteBox();
        testBlackBox();
        testTimeoutDoesNotPoison();
        testZeroTimeout();
        testInterrupt();
        testNullExchange();
        testThirdThreadQueues();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
