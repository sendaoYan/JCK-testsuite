/*
 * @test
 * @summary java.util.concurrent.PriorityBlockingQueue conformance: white-box + black-box
 *          (natural/comparator priority ordering, poll/peek/take, unbounded
 *          remainingCapacity, blocking take handoff, drainTo, null rejection).
 * @run main PriorityBlockingQueueTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PriorityBlockingQueueTest {
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
        Class<?> c = Class.forName("java.util.concurrent.PriorityBlockingQueue");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractQueue.class, "extends AbstractQueue");
        check(BlockingQueue.class.isAssignableFrom(c), "implements BlockingQueue");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null && c.getConstructor(int.class) != null
              && c.getConstructor(int.class, Comparator.class) != null, "constructors");
        check(hasMethod(c, "add", Object.class) && hasMethod(c, "offer", Object.class), "add/offer");
        check(hasMethod(c, "put", Object.class) && hasMethod(c, "take"), "put/take");
        check(hasMethod(c, "poll") && hasMethod(c, "peek"), "poll/peek");
        check(hasMethod(c, "comparator"), "comparator");
        check(hasMethod(c, "remainingCapacity"), "remainingCapacity");
        check(hasMethod(c, "drainTo", java.util.Collection.class), "drainTo");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testNaturalOrdering() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        q.offer(3);
        q.offer(1);
        q.offer(4);
        q.offer(1);
        q.offer(5);
        check(q.size() == 5, "size");
        check(q.peek() == 1, "peek smallest");
        check(q.poll() == 1, "poll 1");
        check(q.poll() == 1, "poll duplicate 1");
        check(q.poll() == 3, "poll 3");
        check(q.poll() == 4, "poll 4");
        check(q.poll() == 5, "poll 5");
        check(q.poll() == null && q.isEmpty(), "empty after polls");
    }

    static void testComparatorOrdering() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>(11, Comparator.reverseOrder());
        q.offer(1);
        q.offer(2);
        q.offer(3);
        check(q.comparator() != null, "comparator non-null");
        check(q.poll() == 3 && q.poll() == 2 && q.poll() == 1, "reverse comparator ordering");
    }

    static void testTakeBlocksWhenEmpty() throws Exception {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
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
        q.put(10);
        done.await();
        check(result.get() == 10, "take blocks until element available");
        t.join();
    }

    static void testDrainToAndRemainingCapacity() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        check(q.remainingCapacity() == Integer.MAX_VALUE, "unbounded remainingCapacity");
        q.add(2);
        q.add(1);
        q.add(3);
        List<Integer> list = new ArrayList<>();
        check(q.drainTo(list) == 3, "drainTo count");
        // PriorityBlockingQueue.drainTo drains in priority (sorted) order.
        check(list.equals(Arrays.asList(1, 2, 3)), "drainTo priority order");
    }

    static void testNullRejection() {
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>();
        expectThrows(() -> q.add(null), NullPointerException.class, "add(null)");
        expectThrows(() -> q.offer(null), NullPointerException.class, "offer(null)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testNaturalOrdering();
        testComparatorOrdering();
        testTakeBlocksWhenEmpty();
        testDrainToAndRemainingCapacity();
        testNullRejection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
