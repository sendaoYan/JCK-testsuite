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

    static void testBlackBox() {
        // constructor(Collection)
        LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>(Arrays.asList(1, 2, 3));
        check(q.size() == 3, "constructor(Collection) copies");

        // toArray / toArray(T[])
        Object[] arr = q.toArray();
        check(arr.length == 3 && arr[0].equals(1), "toArray()");
        Integer[] typed = q.toArray(new Integer[0]);
        check(typed.length == 3 && typed[2] == 3, "toArray(T[])");

        // forEach
        List<Integer> seen = new ArrayList<>();
        q.forEach(seen::add);
        check(seen.equals(Arrays.asList(1, 2, 3)), "forEach order");

        // iterator
        java.util.Iterator<Integer> it = q.iterator();
        check(it.next() == 1 && it.next() == 2 && it.next() == 3, "iterator order");

        // remove(Object)
        check(q.remove(2), "remove(Object) true");
        check(!q.remove(99), "remove(Object) missing false");
        check(q.size() == 2, "size after remove");

        // removeAll / retainAll / removeIf
        q.clear();
        q.addAll(Arrays.asList(1, 2, 3, 4, 5));
        check(q.removeAll(Arrays.asList(1, 3, 5)), "removeAll true");
        check(q.size() == 2 && q.contains(2) && q.contains(4), "removeAll result");
        check(q.retainAll(Arrays.asList(2)), "retainAll true");
        check(q.size() == 1 && q.contains(2), "retainAll result");
        q.add(3);
        q.removeIf(x -> x % 2 == 0);
        check(q.size() == 1 && q.contains(3), "removeIf removes even");

        // spliterator
        LinkedBlockingQueue<Integer> spq = new LinkedBlockingQueue<>(Arrays.asList(1, 2, 3));
        java.util.Spliterator<Integer> sp = spq.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize");
        List<Integer> viaSp = new ArrayList<>();
        sp.forEachRemaining(viaSp::add);
        check(viaSp.equals(Arrays.asList(1, 2, 3)), "spliterator order");

        // clear
        spq.clear();
        check(spq.isEmpty(), "clear empties");

        // toString
        LinkedBlockingQueue<Integer> ts = new LinkedBlockingQueue<>(Arrays.asList(1, 2));
        check(ts.toString() != null && ts.toString().contains("1"), "toString non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testUnboundedAndBounded();
        testTakeBlocksWhenEmpty();
        testPutBlocksWhenFull();
        testDrainTo();
        testNullRejection();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
