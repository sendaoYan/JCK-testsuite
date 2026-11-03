/*
 * @test
 * @summary java.util.concurrent.ArrayBlockingQueue conformance: white-box + black-box
 *          (bounded offer/poll/peek, remainingCapacity, blocking put/take handoff via
 *          another thread + CountDownLatch, offer(timeout), drainTo, clear).
 * @run main ArrayBlockingQueueTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ArrayBlockingQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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
        Class<?> c = Class.forName("java.util.concurrent.ArrayBlockingQueue");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractQueue.class, "extends AbstractQueue");
        check(BlockingQueue.class.isAssignableFrom(c), "implements BlockingQueue");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor(int.class) != null
              && c.getConstructor(int.class, boolean.class) != null, "constructors (int)/(int,boolean)");
        check(hasMethod(c, "add", Object.class) && hasMethod(c, "offer", Object.class), "add/offer");
        check(hasMethod(c, "put", Object.class), "put");
        check(hasMethod(c, "poll") && hasMethod(c, "take"), "poll/take");
        check(hasMethod(c, "offer", Object.class, long.class, TimeUnit.class)
              && hasMethod(c, "poll", long.class, TimeUnit.class), "offer(timeout)/poll(timeout)");
        check(hasMethod(c, "peek") && hasMethod(c, "size"), "peek/size");
        check(hasMethod(c, "remainingCapacity"), "remainingCapacity");
        check(hasMethod(c, "drainTo", java.util.Collection.class), "drainTo");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBoundedSemantics() {
        ArrayBlockingQueue<String> q = new ArrayBlockingQueue<>(2);
        check(q.remainingCapacity() == 2, "remainingCapacity initial");
        check(q.offer("a") && q.offer("b"), "offer within capacity");
        check(!q.offer("c"), "offer beyond capacity false");
        check(q.remainingCapacity() == 0, "remainingCapacity when full");
        check("a".equals(q.peek()), "peek head");
        check("a".equals(q.poll()), "poll");
        check(q.remainingCapacity() == 1, "remainingCapacity after poll");
        check(q.size() == 1, "size");
        q.clear();
        check(q.isEmpty() && q.remainingCapacity() == 2, "clear empties");
    }

    static void testTakeBlocksWhenEmpty() throws Exception {
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(2);
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
        // take() blocks on the empty queue; put() unblocks it.
        q.put(42);
        done.await();
        check(result.get() == 42, "take blocks until element available");
        t.join();
    }

    static void testPutBlocksWhenFull() throws Exception {
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(1);
        q.put(1); // fills the single slot
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
        check(q.peek() == 1, "queue still holds 1 while put is blocked");
        check(q.take() == 1, "take frees a slot");
        done.await();
        check(completed.get(), "put completed after space freed");
        check(q.peek() == 2, "queue now holds 2");
        t.join();
    }

    static void testOfferTimeoutImmediate() throws Exception {
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(1);
        q.add(1);
        check(!q.offer(2, 1, TimeUnit.NANOSECONDS), "offer(timeout) false when full");
        check(q.poll(1, TimeUnit.NANOSECONDS) == 1, "poll(timeout) returns existing element");
        check(q.poll(1, TimeUnit.NANOSECONDS) == null, "poll(timeout) null when empty");
    }

    static void testDrainTo() {
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(5);
        q.add(1);
        q.add(2);
        q.add(3);
        List<Integer> list = new ArrayList<>();
        check(q.drainTo(list) == 3, "drainTo count");
        check(list.equals(Arrays.asList(1, 2, 3)), "drainTo FIFO order");
        check(q.isEmpty(), "empty after drainTo");
        q.add(4);
        q.add(5);
        List<Integer> list2 = new ArrayList<>();
        check(q.drainTo(list2, 1) == 1, "drainTo(maxElements) count");
        check(list2.equals(Arrays.asList(4)) && q.size() == 1, "drainTo limited");
    }

    static void testFairConstructor() {
        ArrayBlockingQueue<Integer> fair = new ArrayBlockingQueue<>(2, true);
        fair.add(1);
        fair.add(2);
        check(fair.size() == 2 && fair.poll() == 1, "fair constructor works");
        ArrayBlockingQueue<Integer> nonFair = new ArrayBlockingQueue<>(2, false);
        nonFair.add(9);
        check(nonFair.peek() == 9, "non-fair constructor works");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBoundedSemantics();
        testTakeBlocksWhenEmpty();
        testPutBlocksWhenFull();
        testOfferTimeoutImmediate();
        testDrainTo();
        testFairConstructor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
