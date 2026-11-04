/*
 * @test
 * @summary java.util.concurrent.TransferQueue conformance: white-box structure + black-box behavior.
 * @run main TransferQueueTest
 */

import java.util.concurrent.*;

public class TransferQueueTest {
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
        Class<?> c = cls("java.util.concurrent.TransferQueue");
        check(c.isInterface(), "TransferQueue is an interface");
        check(BlockingQueue.class.isAssignableFrom(c), "extends BlockingQueue");
        hasMethod(c, "transfer", Object.class);
        hasMethod(c, "tryTransfer", Object.class);
        hasMethod(c, "tryTransfer", Object.class, long.class, TimeUnit.class);
        hasMethod(c, "hasWaitingConsumer");
        hasMethod(c, "getWaitingConsumerCount");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        TransferQueue<String> q = new LinkedTransferQueue<>();
        check(q.getWaitingConsumerCount() == 0, "no waiting consumers initially");
        check(!q.hasWaitingConsumer(), "hasWaitingConsumer false initially");
        check(!q.tryTransfer("x"), "tryTransfer fails with no consumer");

        q.put("a");
        check("a".equals(q.take()), "put/take FIFO");
        check(q.isEmpty(), "empty after take");

        // a consumer waiting on take() lets transfer() hand off directly
        CountDownLatch consumed = new CountDownLatch(1);
        final String[] got = new String[1];
        Thread consumer = new Thread(() -> {
            try { got[0] = q.take(); consumed.countDown(); }
            catch (InterruptedException e) { throw new AssertionError(e); }
        });
        consumer.start();
        while (q.getWaitingConsumerCount() == 0) { Thread.yield(); }
        q.transfer("z");
        check(consumed.await(5, TimeUnit.SECONDS), "transfer hands off to waiting consumer");
        consumer.join(5000);
        check("z".equals(got[0]), "consumer received transferred element");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
