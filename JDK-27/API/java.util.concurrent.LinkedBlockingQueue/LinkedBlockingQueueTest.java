/*
 * @test
 * @summary java.util.concurrent.LinkedBlockingQueue conformance: white-box + black-box
 *          (unbounded/bounded offer/poll/peek, remainingCapacity, blocking put/take
 *          handoff, drainTo, null rejection).
 * @run main LinkedBlockingQueueTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkedBlockingQueueTest {
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
        Class<?> c = Class.forName("java.util.concurrent.LinkedBlockingQueue");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractQueue.class, "extends AbstractQueue");
        check(BlockingQueue.class.isAssignableFrom(c), "implements BlockingQueue");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null && c.getConstructor(int.class) != null,
              "constructors ()/(int)");
        check(hasMethod(c, "put", Object.class) && hasMethod(c, "take"), "put/take");
        check(hasMethod(c, "offer", Object.class) && hasMethod(c, "poll"), "offer/poll");
        check(hasMethod(c, "offer", Object.class, long.class, TimeUnit.class)
              && hasMethod(c, "poll", long.class, TimeUnit.class), "offer(timeout)/poll(timeout)");
        check(hasMethod(c, "peek") && hasMethod(c, "size"), "peek/size");
        check(hasMethod(c, "remainingCapacity"), "remainingCapacity");
        check(hasMethod(c, "drainTo", java.util.Collection.class), "drainTo");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testUnboundedAndBounded() {
        LinkedBlockingQueue<String> unbounded = new LinkedBlockingQueue<>();
        check(unbounded.remainingCapacity() == Integer.MAX_VALUE, "unbounded remainingCapacity");
        unbounded.offer("a");
        unbounded.offer("b");
        check(unbounded.size() == 2, "unbounded size");
        check("a".equals(unbounded.poll()) && "b".equals(unbounded.poll()), "unbounded FIFO");

        LinkedBlockingQueue<String> q = new LinkedBlockingQueue<>(2);
        check(q.offer("a") && q.offer("b"), "offer within capacity");
        check(!q.offer("c"), "offer beyond capacity false");
        check(q.remainingCapacity() == 0, "remainingCapacity when full");
        check("a".equals(q.poll()), "poll");
        check(q.size() == 1 && "b".equals(q.peek()), "size/peek");
    }

    static void testTakeBlocksWhenEmpty() throws Exception {
        LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>();
        AtomicInteger result = new AtomicInteger(-1);
        CountDownLatch done = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            try {
                result.set(q.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });
        q.put(7);
        done.await();
        check(result.get() == 7, "take blocks until element available");
        t.join();
    }

    static void testPutBlocksWhenFull() throws Exception {
        LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>(1);
        q.put(1);
        AtomicBoolean completed = new AtomicBoolean(false);
        CountDownLatch done = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            try {
                q.put(2);
                completed.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });
        check(q.take() == 1, "take frees slot for blocked put");
        done.await();
        check(completed.get() && q.peek() == 2, "put completed after slot freed");
        t.join();
    }

    static void testDrainTo() {
        LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>();
        q.add(1);
        q.add(2);
        q.add(3);
        List<Integer> list = new ArrayList<>();
        check(q.drainTo(list) == 3, "drainTo count");
        check(list.equals(Arrays.asList(1, 2, 3)), "drainTo FIFO order");
        check(q.isEmpty(), "empty after drainTo");
    }

    static void testNullRejection() {
        LinkedBlockingQueue<String> q = new LinkedBlockingQueue<>();
        expectThrows(() -> q.add(null), NullPointerException.class, "add(null)");
        expectThrows(() -> q.offer(null), NullPointerException.class, "offer(null)");
        check(!q.contains(null), "contains(null) returns false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testUnboundedAndBounded();
        testTakeBlocksWhenEmpty();
        testPutBlocksWhenFull();
        testDrainTo();
        testNullRejection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
