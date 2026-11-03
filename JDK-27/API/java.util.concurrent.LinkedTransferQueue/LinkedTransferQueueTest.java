/*
 * @test
 * @summary java.util.concurrent.LinkedTransferQueue conformance: white-box + black-box
 *          (offer/poll FIFO, transfer blocks until taken, tryTransfer immediate false,
 *          waiting-consumer introspection, drainTo, null rejection).
 * @run main LinkedTransferQueueTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class LinkedTransferQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                                         + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    static Thread startDaemon(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
        return t;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.LinkedTransferQueue");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractQueue.class, "extends AbstractQueue");
        check(TransferQueue.class.isAssignableFrom(c), "implements TransferQueue");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null && c.getConstructor(java.util.Collection.class) != null,
              "constructors ()/(Collection)");
        check(hasMethod(c, "put", Object.class) && hasMethod(c, "take"), "put/take");
        check(hasMethod(c, "offer", Object.class) && hasMethod(c, "poll"), "offer/poll");
        check(hasMethod(c, "transfer", Object.class) && hasMethod(c, "tryTransfer", Object.class),
              "transfer/tryTransfer");
        check(hasMethod(c, "tryTransfer", Object.class, long.class, java.util.concurrent.TimeUnit.class),
              "tryTransfer(E,long,TimeUnit)");
        check(hasMethod(c, "hasWaitingConsumer") && hasMethod(c, "getWaitingConsumerCount"),
              "hasWaitingConsumer/getWaitingConsumerCount");
        check(hasMethod(c, "peek") && hasMethod(c, "size"), "peek/size");
        check(hasMethod(c, "drainTo", java.util.Collection.class), "drainTo");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testFifoOfferPoll() {
        LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        check(q.isEmpty() && q.size() == 0, "empty initially");
        check(q.offer(1) && q.add(2) && q.offer(3), "offer/add");
        check(q.size() == 3, "size");
        check(q.peek() == 1, "peek head");
        check(q.poll() == 1 && q.poll() == 2 && q.poll() == 3, "FIFO poll");
        check(q.poll() == null && q.isEmpty(), "empty after polls");
    }

    static void testTransferBlocksUntilTaken() throws Exception {
        LinkedTransferQueue<String> q = new LinkedTransferQueue<>();
        AtomicReference<String> received = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            try {
                received.set(q.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });
        q.transfer("data"); // blocks until take() receives it
        done.await();
        check("data".equals(received.get()), "transfer hands off to blocking take");
        t.join();
        check(q.isEmpty(), "empty after transfer");
    }

    static void testTryTransferImmediate() {
        LinkedTransferQueue<String> q = new LinkedTransferQueue<>();
        check(!q.tryTransfer("x"), "tryTransfer false with no waiting consumer");
        check(q.isEmpty(), "tryTransfer did not enqueue");
    }

    static void testWaitingConsumerIntrospection() {
        LinkedTransferQueue<String> q = new LinkedTransferQueue<>();
        check(!q.hasWaitingConsumer(), "no waiting consumer initially");
        check(q.getWaitingConsumerCount() == 0, "waiting consumer count 0 initially");
    }

    static void testDrainToAndNull() {
        LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        q.add(1);
        q.add(2);
        q.add(3);
        List<Integer> list = new ArrayList<>();
        check(q.drainTo(list) == 3, "drainTo count");
        check(list.equals(Arrays.asList(1, 2, 3)), "drainTo FIFO order");
        check(q.isEmpty(), "empty after drainTo");
        expectThrows(() -> q.add(null), NullPointerException.class, "add(null)");
        expectThrows(() -> q.offer(null), NullPointerException.class, "offer(null)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testFifoOfferPoll();
        testTransferBlocksUntilTaken();
        testTryTransferImmediate();
        testWaitingConsumerIntrospection();
        testDrainToAndNull();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
