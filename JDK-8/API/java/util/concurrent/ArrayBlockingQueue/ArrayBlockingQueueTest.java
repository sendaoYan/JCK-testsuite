/*
 * @test
 * @summary java.util.concurrent.ArrayBlockingQueue conformance: white-box + black-box
 *          (bounded offer/poll/peek, remainingCapacity, blocking put/take handoff via
 *          another thread + CountDownLatch, offer(timeout), drainTo, clear, null-element
 *          rejection, toArray contract, removeAll/retainAll/removeIf, spliterator).
 * @run main ArrayBlockingQueueTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
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

    interface ThrowingRunnable { void run() throws Throwable; }

    static void expectThrows(Class<? extends Throwable> type, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                    + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
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

    static void testNullRejection() {
        ArrayBlockingQueue<String> q = new ArrayBlockingQueue<>(2);
        expectThrows(NullPointerException.class, () -> q.add(null), "add(null)");
        expectThrows(NullPointerException.class, () -> q.offer(null), "offer(null)");
        expectThrows(NullPointerException.class, () -> q.put(null), "put(null)");
        expectThrows(NullPointerException.class, () -> q.offer(null, 1, TimeUnit.SECONDS), "offer(null,timeout)");
        check(!q.contains(null), "contains(null) returns false");
        check(!q.remove(null), "remove(null) returns false");
        expectThrows(NullPointerException.class, () -> q.drainTo(null), "drainTo(null)");
        check(q.isEmpty(), "queue unchanged after null rejections");
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

    static void testBlackBox() throws InterruptedException {
        // Constructor (int, boolean, Collection)
        List<String> seed = Arrays.asList("a", "b");
        ArrayBlockingQueue<String> q = new ArrayBlockingQueue<>(4, true, seed);
        check(q.size() == 2, "constructor(Collection) copies elements");
        check(q.contains("a") && q.contains("b"), "contains after constructor");
        check(!q.contains("z"), "contains missing false");
        q.add("c");

        // toArray()/toArray(T[])
        Object[] arr = q.toArray();
        check(arr.length == 3 && "a".equals(arr[0]) && "c".equals(arr[2]), "toArray() FIFO");
        String[] typed = q.toArray(new String[0]);
        check(typed.length == 3 && "a".equals(typed[0]), "toArray(T[])");
        String[] sized = q.toArray(new String[5]);
        check(sized.length == 5 && sized[3] == null, "toArray(T[bigger]) pads null");

        // forEach preserves FIFO order
        List<String> seen = new ArrayList<>();
        q.forEach(seen::add);
        check(seen.equals(Arrays.asList("a", "b", "c")), "forEach FIFO order");

        // iterator order + remove via iterator
        java.util.Iterator<String> it = q.iterator();
        check(it.hasNext() && "a".equals(it.next()), "iterator next");
        it.remove();
        check(!q.contains("a") && q.size() == 2, "iterator.remove removes element");

        // remove(Object)
        check(q.remove("b"), "remove(Object) true");
        check(!q.remove("b"), "remove(Object) missing false");

        // removeAll / retainAll
        q.clear();
        q.addAll(Arrays.asList("x", "y", "z"));
        check(q.removeAll(Arrays.asList("x", "z")), "removeAll true");
        check(q.size() == 1 && "y".equals(q.peek()), "removeAll result");
        q.clear();
        q.addAll(Arrays.asList("x", "y", "z"));
        check(q.retainAll(Arrays.asList("x", "y")), "retainAll true");
        check(q.size() == 2 && q.contains("x") && q.contains("y"), "retainAll result");

        // removeIf
        q.removeIf(s -> s.equals("x"));
        check(q.size() == 1 && "y".equals(q.peek()), "removeIf removes matching");

        // spliterator
        q.clear();
        q.addAll(Arrays.asList("1", "2", "3"));
        java.util.Spliterator<String> sp = q.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize");
        check(sp.hasCharacteristics(java.util.Spliterator.ORDERED), "spliterator ORDERED");
        List<String> viaSp = new ArrayList<>();
        sp.forEachRemaining(viaSp::add);
        check(viaSp.equals(Arrays.asList("1", "2", "3")), "spliterator forEachRemaining order");

        // toString
        check(q.toString() != null && q.toString().contains("1"), "toString non-null with content");

        // ---- 边界值/等价类/等价性 ----
        expectThrows(IllegalArgumentException.class, () -> new ArrayBlockingQueue<>(0),
                "capacity 0 constructor");
        expectThrows(IllegalArgumentException.class, () -> new ArrayBlockingQueue<>(-1),
                "capacity negative constructor");

        ArrayBlockingQueue<String> empty = new ArrayBlockingQueue<>(2);
        expectThrows(NoSuchElementException.class, () -> empty.element(), "element() empty");
        expectThrows(NoSuchElementException.class, () -> empty.remove(), "remove() empty");
        check(empty.poll() == null && empty.peek() == null, "poll/peek empty null");

        ArrayBlockingQueue<String> full = new ArrayBlockingQueue<>(2);
        full.add("a");
        full.add("b");
        expectThrows(IllegalStateException.class, () -> full.add("c"), "add() full");
        check(full.size() == 2, "size unchanged after failed add");
        check(!full.offer("c", 0, TimeUnit.NANOSECONDS), "offer(timeout 0) full false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBoundedSemantics();
        testNullRejection();
        testTakeBlocksWhenEmpty();
        testPutBlocksWhenFull();
        testOfferTimeoutImmediate();
        testDrainTo();
        testFairConstructor();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
