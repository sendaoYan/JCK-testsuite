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

    static void testBlackBox() {
        // constructors (int)/(Collection)
        PriorityBlockingQueue<Integer> c1 = new PriorityBlockingQueue<>(16);
        check(c1.isEmpty(), "constructor(int) empty");
        PriorityBlockingQueue<Integer> c2 = new PriorityBlockingQueue<>(Arrays.asList(3, 1, 2));
        check(c2.size() == 3 && c2.peek() == 1, "constructor(Collection) heapified");

        // contains
        check(c2.contains(2) && !c2.contains(99), "contains");

        // toArray / toArray(T[])
        Object[] arr = c2.toArray();
        Arrays.sort(arr);
        check(arr.length == 3 && arr[0].equals(1) && arr[2].equals(3), "toArray() contents");
        Integer[] typed = c2.toArray(new Integer[0]);
        check(typed.length == 3, "toArray(T[]) length");

        // forEach
        List<Integer> seen = new ArrayList<>();
        c2.forEach(seen::add);
        seen.sort(Integer::compareTo);
        check(seen.equals(Arrays.asList(1, 2, 3)), "forEach contents");

        // iterator enumerates all (heap order, not sorted)
        java.util.Iterator<Integer> it = c2.iterator();
        int n = 0;
        while (it.hasNext()) { it.next(); n++; }
        check(n == 3, "iterator enumerates all");

        // remove(Object)
        check(c2.remove(2), "remove(Object) true");
        check(!c2.remove(99), "remove(Object) missing false");

        // removeAll / retainAll / removeIf
        PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>(Arrays.asList(1, 2, 3, 4, 5));
        check(q.removeAll(Arrays.asList(1, 3, 5)), "removeAll true");
        check(q.size() == 2 && q.contains(2) && q.contains(4), "removeAll result");
        check(q.retainAll(Arrays.asList(2)), "retainAll true");
        check(q.size() == 1 && q.contains(2), "retainAll result");
        q.add(3);
        q.removeIf(x -> x % 2 == 0);
        check(q.size() == 1 && q.contains(3), "removeIf removes even");

        // spliterator
        PriorityBlockingQueue<Integer> spq = new PriorityBlockingQueue<>(Arrays.asList(3, 1, 2));
        java.util.Spliterator<Integer> sp = spq.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize");
        List<Integer> viaSp = new ArrayList<>();
        sp.forEachRemaining(viaSp::add);
        viaSp.sort(Integer::compareTo);
        check(viaSp.equals(Arrays.asList(1, 2, 3)), "spliterator contents");

        // clear
        spq.clear();
        check(spq.isEmpty(), "clear empties");

        // toString
        PriorityBlockingQueue<Integer> ts = new PriorityBlockingQueue<>(Arrays.asList(1, 2));
        check(ts.toString() != null && ts.toString().contains("1"), "toString non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testNaturalOrdering();
        testComparatorOrdering();
        testTakeBlocksWhenEmpty();
        testDrainToAndRemainingCapacity();
        testNullRejection();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
