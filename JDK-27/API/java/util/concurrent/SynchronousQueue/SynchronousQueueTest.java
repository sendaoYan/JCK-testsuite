/*
 * @test
 * @summary java.util.concurrent.SynchronousQueue conformance: white-box + black-box
 *          (rendezvous handoff: put blocks until take, no internal capacity (size/empty/
 *          remainingCapacity always 0/true/0), non-blocking offer/poll immediate false/
 *          null, fair constructor).
 * @run main SynchronousQueueTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SynchronousQueueTest {
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
        Class<?> c = Class.forName("java.util.concurrent.SynchronousQueue");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractQueue.class, "extends AbstractQueue");
        check(BlockingQueue.class.isAssignableFrom(c), "implements BlockingQueue");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null && c.getConstructor(boolean.class) != null,
              "constructors ()/(boolean)");
        check(hasMethod(c, "put", Object.class) && hasMethod(c, "take"), "put/take");
        check(hasMethod(c, "offer", Object.class) && hasMethod(c, "poll"), "offer/poll");
        check(hasMethod(c, "offer", Object.class, long.class, TimeUnit.class)
              && hasMethod(c, "poll", long.class, TimeUnit.class), "offer(timeout)/poll(timeout)");
        check(hasMethod(c, "size") && hasMethod(c, "isEmpty")
              && hasMethod(c, "remainingCapacity"), "size/isEmpty/remainingCapacity");
        check(hasMethod(c, "peek"), "peek");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testNoCapacity() {
        SynchronousQueue<String> q = new SynchronousQueue<>();
        check(q.size() == 0, "size always 0");
        check(q.isEmpty(), "isEmpty always true");
        check(q.remainingCapacity() == 0, "remainingCapacity always 0");
        check(q.peek() == null, "peek always null");
        check(q.poll() == null, "poll always null with no producer");
        check(!q.offer("x"), "offer always false with no consumer");
        q.clear(); // no-op, must not throw
        check(q.size() == 0, "size still 0 after clear");
    }

    static void testHandoff() throws Exception {
        SynchronousQueue<String> q = new SynchronousQueue<>();
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
        // take() blocks until a put() hands off the element.
        q.put("hello");
        done.await();
        check("hello".equals(received.get()), "put hands off to blocking take");
        t.join();
        check(q.isEmpty(), "still empty after handoff");
    }

    static void testPutBlocksUntilTake() throws Exception {
        SynchronousQueue<String> q = new SynchronousQueue<>();
        AtomicReference<String> received = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Thread producer = startDaemon(() -> {
            try {
                q.put("data");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });
        // put blocks; take unblocks it by receiving the value.
        received.set(q.take());
        done.await();
        check("data".equals(received.get()), "take receives value from blocking put");
        producer.join();
    }

    static void testImmediateTimeout() throws Exception {
        SynchronousQueue<String> q = new SynchronousQueue<>();
        check(!q.offer("x", 1, TimeUnit.NANOSECONDS), "offer(timeout) false with no consumer");
        check(q.poll(1, TimeUnit.NANOSECONDS) == null, "poll(timeout) null with no producer");
    }

    static void testFairConstructor() {
        SynchronousQueue<String> fair = new SynchronousQueue<>(true);
        check(fair.isEmpty() && fair.size() == 0, "fair constructor works");
        SynchronousQueue<String> nonFair = new SynchronousQueue<>(false);
        check(nonFair.isEmpty(), "non-fair constructor works");
    }

    static void testBlackBox() {
        SynchronousQueue<String> q = new SynchronousQueue<>();
        check(!q.contains("x"), "contains always false");
        check(!q.containsAll(java.util.Arrays.asList("x")), "containsAll non-empty false");
        check(q.containsAll(new java.util.ArrayList<String>()), "containsAll empty collection true");

        // remove / removeAll / retainAll
        check(!q.remove("x"), "remove(Object) false");
        check(!q.removeAll(java.util.Arrays.asList("x")), "removeAll false");
        check(!q.retainAll(java.util.Arrays.asList("x")), "retainAll false");

        // drainTo
        java.util.List<String> drained = new java.util.ArrayList<>();
        check(q.drainTo(drained) == 0, "drainTo returns 0");
        check(q.drainTo(drained, 10) == 0 && drained.isEmpty(), "drainTo(max) returns 0");

        // iterator
        java.util.Iterator<String> it = q.iterator();
        check(!it.hasNext(), "iterator has no elements");

        // spliterator
        java.util.Spliterator<String> sp = q.spliterator();
        check(sp.estimateSize() == 0, "spliterator estimateSize 0");

        // toArray / toArray(T[])
        check(q.toArray().length == 0, "toArray() empty");
        check(q.toArray(new String[0]).length == 0, "toArray(T[]) empty");

        // toString
        check(q.toString() != null && q.toString().contains("["), "toString non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testNoCapacity();
        testHandoff();
        testPutBlocksUntilTake();
        testImmediateTimeout();
        testFairConstructor();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
