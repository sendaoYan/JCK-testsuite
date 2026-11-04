/*
 * @test
 * @summary java.util.concurrent.LinkedBlockingQueue conformance: white-box + black-box
 *          (unbounded/bounded offer/poll/peek, remainingCapacity, blocking put/take
 *          handoff, drainTo, null rejection, six-method two-state table with exact
 *          "Queue full" message, interrupted take/put, iterator remove + weak
 *          consistency, serialization round-trip preserving capacity, identity equals,
 *          toArray contract, addAll overflow prefix retention).
 * @run main LinkedBlockingQueueTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
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

    @SuppressWarnings("unchecked")
    static <T extends Throwable> T expectThrowsMsg(Runnable r, Class<T> type, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return type.cast(t); }
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

    static void awaitBounded(CountDownLatch latch, String what) throws InterruptedException {
        if (!latch.await(5, TimeUnit.SECONDS))
            throw new AssertionError("timeout (5s) waiting for: " + what);
    }

    // preserved original smoke test
    static void testLinkedBlockingQueue() {
        LinkedBlockingQueue<String> q = new LinkedBlockingQueue<>(2);
        check(q.offer("a") && q.offer("b"), "offer");
        check(!q.offer("c"), "offer beyond capacity returns false");
        check("a".equals(q.poll()), "poll");
        check(q.size() == 1, "size");
        check("b".equals(q.peek()), "peek");
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

    static void testBlackBox() throws InterruptedException {
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

        // ---- 边界值/等价类/等价性 ----
        // 容量 0/负数边界（非法）
        expectThrows(() -> new LinkedBlockingQueue<>(0), IllegalArgumentException.class,
                "capacity 0 constructor");
        expectThrows(() -> new LinkedBlockingQueue<>(-1), IllegalArgumentException.class,
                "capacity negative constructor");

        // 空队列查询边界
        LinkedBlockingQueue<Integer> empty = new LinkedBlockingQueue<>();
        expectThrows(() -> empty.element(), NoSuchElementException.class, "element() empty");
        expectThrows(() -> empty.remove(), NoSuchElementException.class, "remove() empty");
        check(empty.poll() == null && empty.peek() == null, "poll/peek empty null");

        // 满队列 add 抛异常
        LinkedBlockingQueue<Integer> full = new LinkedBlockingQueue<>(2);
        full.add(1);
        full.add(2);
        expectThrows(() -> full.add(3), IllegalStateException.class, "add() full");
        check(!full.offer(3, 0, TimeUnit.NANOSECONDS), "offer(timeout 0) full false");

        // 无界 offer 永不失败 + remainingCapacity 随计数递减
        LinkedBlockingQueue<Integer> unb = new LinkedBlockingQueue<>();
        check(unb.offer(1) && unb.offer(2), "unbounded offer always true");
        check(unb.remainingCapacity() == Integer.MAX_VALUE - 2,
                "unbounded remainingCapacity after 2");
    }

    // ---- Part 3: blocking-queue contract deepening ----
    static void testSixMethodTwoStateTable() {
        LinkedBlockingQueue<String> q = new LinkedBlockingQueue<>(2);
        // empty state
        check(q.isEmpty() && q.size() == 0, "two-state: initially empty");
        expectThrows(() -> q.remove(), NoSuchElementException.class, "two-state: remove() empty NSEE");
        expectThrows(() -> q.element(), NoSuchElementException.class, "two-state: element() empty NSEE");
        check(q.poll() == null, "two-state: poll() empty null");
        check(q.peek() == null, "two-state: peek() empty null");
        check(q.add("a") && q.add("b"), "two-state: add succeeds while space remains");
        // full state
        IllegalStateException ise = expectThrowsMsg(() -> q.add("c"),
                IllegalStateException.class, "two-state: add() full ISE");
        check("Queue full".equals(ise.getMessage()), "two-state: add() full message exactly 'Queue full'");
        check(!q.offer("c"), "two-state: offer() full false");
        check(q.size() == 2, "two-state: failed insert leaves size unchanged");
        check("a".equals(q.element()) && "a".equals(q.peek()), "two-state: element/peek full return head");
        check("a".equals(q.remove()) && "b".equals(q.remove()), "two-state: remove() drains FIFO");
    }

    static void testConstructorIaeMessage() {
        IllegalArgumentException e0 = expectThrowsMsg(() -> new LinkedBlockingQueue<String>(0),
                IllegalArgumentException.class, "ctor capacity 0 IAE");
        check(e0.getMessage() == null, "ctor capacity 0 IAE has null message");
        IllegalArgumentException en = expectThrowsMsg(() -> new LinkedBlockingQueue<String>(-1),
                IllegalArgumentException.class, "ctor capacity negative IAE");
        check(en.getMessage() == null, "ctor negative IAE has null message");
        LinkedBlockingQueue<Integer> one = new LinkedBlockingQueue<>(1);
        check(one.remainingCapacity() == 1 && one.offer(1) && !one.offer(2), "capacity 1 constructor usable");
    }

    static void testUnboundedCapacityGrowth() {
        LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>();
        boolean all = true;
        for (int i = 0; i < 100; i++) all &= q.offer(i);
        check(all, "unbounded offer of 100 elements never fails");
        check(q.size() == 100, "size after 100 offers");
        check(q.remainingCapacity() == Integer.MAX_VALUE - 100, "remainingCapacity = MAX - size");
        boolean fifo = true;
        for (int i = 0; i < 100; i++) fifo &= (q.poll() == i);
        check(fifo, "100 elements drain in FIFO order");
        check(q.isEmpty(), "empty after draining 100");
    }

    static void testDrainToEdges() {
        LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>(4);
        q.add(1);
        q.add(2);
        List<Integer> sink = new ArrayList<>();
        check(q.drainTo(sink, 0) == 0, "drainTo(max=0) returns 0");
        check(sink.isEmpty() && q.size() == 2, "drainTo(max=0) mutates nothing");
        IllegalArgumentException iae = expectThrowsMsg(() -> q.drainTo(q),
                IllegalArgumentException.class, "drainTo(self) IAE");
        check(iae.getMessage() == null, "drainTo(self) IAE has null message");
        check(q.size() == 2, "drainTo(self) leaves queue unchanged");
        q.add(3);
        List<Integer> part = new ArrayList<>();
        check(q.drainTo(part, 2) == 2 && part.equals(Arrays.asList(1, 2)),
                "drainTo(max) truncates at max in FIFO order");
        check(q.size() == 1 && q.peek() == 3, "drainTo(max) leaves remainder");
        List<Integer> rest = new ArrayList<>();
        check(q.drainTo(rest) == 1 && rest.equals(Arrays.asList(3)) && q.isEmpty(),
                "drainTo remainder drains with source cleared");
    }

    static void testTimedOfferWaitsWhenFull() throws Exception {
        LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>(1);
        q.add(1);
        long t0 = System.nanoTime();
        check(!q.offer(2, 200, TimeUnit.MILLISECONDS), "timed offer on full waits then returns false");
        check(System.nanoTime() - t0 >= 150_000_000L, "timed offer waited ~200ms, not immediate");
        q.clear();
        t0 = System.nanoTime();
        check(q.poll(200, TimeUnit.MILLISECONDS) == null, "timed poll on empty waits then returns null");
        check(System.nanoTime() - t0 >= 150_000_000L, "timed poll waited ~200ms, not immediate");
    }

    static void testInterruptedTake() throws Exception {
        LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>();
        CountDownLatch entered = new CountDownLatch(1), done = new CountDownLatch(1);
        AtomicBoolean gotIE = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            entered.countDown();
            try {
                q.take();
            } catch (InterruptedException e) {
                gotIE.set(true);
            } finally {
                done.countDown();
            }
        });
        awaitBounded(entered, "take thread entry");
        t.interrupt();
        awaitBounded(done, "interrupted take completion");
        t.join(5000);
        check(gotIE.get(), "blocked take interrupted -> InterruptedException");
        check(!t.isAlive(), "interrupted take thread terminated");
    }

    static void testInterruptStatusAtEntry() throws Exception {
        LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>();
        CountDownLatch done = new CountDownLatch(1);
        AtomicBoolean gotIE = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            Thread.currentThread().interrupt(); // status set before take()
            try {
                q.take();
            } catch (InterruptedException e) {
                gotIE.set(true);
            } finally {
                done.countDown();
            }
        });
        awaitBounded(done, "pre-interrupted take completion");
        t.join(5000);
        check(gotIE.get(), "take with pre-set interrupt status throws InterruptedException");
    }

    static void testInterruptedPut() throws Exception {
        LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>(1);
        q.add(1);
        CountDownLatch entered = new CountDownLatch(1), done = new CountDownLatch(1);
        AtomicBoolean gotIE = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            entered.countDown();
            try {
                q.put(2);
            } catch (InterruptedException e) {
                gotIE.set(true);
            } finally {
                done.countDown();
            }
        });
        awaitBounded(entered, "put thread entry");
        t.interrupt();
        awaitBounded(done, "interrupted put completion");
        t.join(5000);
        check(gotIE.get(), "blocked put interrupted -> InterruptedException");
        check(q.size() == 1 && q.peek() == 1, "queue unchanged after interrupted put");
    }

    static void testIteratorRemoveAndWeakConsistency() {
        LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>(4);
        q.add(1);
        q.add(2);
        q.add(3);
        java.util.Iterator<Integer> it = q.iterator();
        check(it.hasNext() && it.next() == 1, "iterator first element");
        it.remove();
        check(q.size() == 2 && q.peek() == 2, "iterator.remove removes current element");
        // weak consistency: fresh queue, first next() consumed, then structural removal
        LinkedBlockingQueue<Integer> w = new LinkedBlockingQueue<>(4);
        w.add(1);
        w.add(2);
        w.add(3);
        java.util.Iterator<Integer> itw = w.iterator();
        check(itw.next() == 1, "weak-consistency iterator first element");
        check(w.remove(Integer.valueOf(3)), "structural removal during iteration");
        List<Integer> rest = new ArrayList<>();
        while (itw.hasNext()) rest.add(itw.next()); // must not throw CME
        check(rest.equals(Arrays.asList(2)), "weakly consistent iterator survives removal, no CME");
    }

    static void testSerializationRoundTrip() throws Exception {
        LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>(3);
        q.add(1);
        q.add(2);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(q);
        }
        Object back;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            back = ois.readObject();
        }
        @SuppressWarnings("unchecked")
        LinkedBlockingQueue<Integer> rt = (LinkedBlockingQueue<Integer>) back;
        check(rt.size() == 2, "serialization round-trip preserves element count");
        check(rt.poll() == 1 && rt.poll() == 2 && rt.isEmpty(), "serialization round-trip preserves FIFO order");
        check(rt.remainingCapacity() == 3, "serialization round-trip preserves capacity");
        check(rt.offer(1) && rt.offer(2) && rt.offer(3), "round-tripped queue accepts capacity elements");
        check(!rt.offer(4), "round-tripped queue is still bounded by original capacity");
    }

    static void testNullRejectionExtended() {
        LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>(2);
        try {
            q.put(null);
            throw new AssertionError("put(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        } catch (InterruptedException e) {
            throw new AssertionError("put(null) interrupted instead of NPE");
        }
        try {
            q.offer(null, 1, TimeUnit.SECONDS);
            throw new AssertionError("offer(null,timeout) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        } catch (InterruptedException e) {
            throw new AssertionError("offer(null,timeout) interrupted instead of NPE");
        }
        expectThrows(() -> q.drainTo(null), NullPointerException.class, "drainTo(null)");
        check(!q.contains(null), "contains(null) false");
        check(!q.remove(null), "remove(null) false");
        check(q.isEmpty(), "queue unchanged after null rejections");
    }

    static void testEqualsIdentitySemantics() {
        LinkedBlockingQueue<Integer> a = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<Integer> b = new LinkedBlockingQueue<>();
        check(a.add(1) && a.add(2) && b.add(1) && b.add(2), "setup: equal contents, distinct instances");
        check(a.equals(a), "equals(self) true");
        check(!a.equals(b), "no element-based equals override (identity semantics)");
        check(!a.equals(null), "equals(null) false");
        check(!a.equals(Arrays.asList(1, 2)), "queue not equal to List with same elements");
    }

    static void testToArrayExactContract() {
        LinkedBlockingQueue<String> q = new LinkedBlockingQueue<>(4);
        q.add("a");
        q.add("b");
        q.add("c");
        Object[] arr = q.toArray();
        check(arr.getClass() == Object[].class && arr.length == 3, "toArray() exact length");
        check(Arrays.equals(arr, new Object[]{"a", "b", "c"}), "toArray() follows iteration order");
        String[] small = q.toArray(new String[0]);
        check(small.getClass() == String[].class, "toArray(T[0]) exact runtime type");
        check(Arrays.equals(small, new String[]{"a", "b", "c"}), "toArray(T[0]) contents in order");
        String[] exact = q.toArray(new String[3]);
        check(exact.getClass() == String[].class && "c".equals(exact[2]), "toArray(T[exact size]) filled");
    }

    static void testContainsRemoveMiddle() {
        LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>(4);
        q.add(1);
        q.add(2);
        q.add(3);
        check(q.contains(2), "contains middle element");
        check(q.remove(Integer.valueOf(2)), "remove middle element");
        check(!q.contains(2) && q.size() == 2, "middle element gone");
        check(Arrays.equals(q.toArray(), new Object[]{1, 3}), "order preserved after middle removal");
    }

    static void testAddAllOverflowPartial() {
        LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>(2);
        IllegalStateException ise = expectThrowsMsg(
                () -> q.addAll(Arrays.asList(1, 2, 3)), IllegalStateException.class,
                "addAll overflow ISE");
        check("Queue full".equals(ise.getMessage()), "addAll overflow message 'Queue full'");
        check(q.size() == 2, "addAll overflow retains already-inserted prefix");
        check(q.poll() == 1 && q.poll() == 2 && q.isEmpty(), "retained prefix in order");
    }

    public static void main(String[] args) throws Exception {
        testLinkedBlockingQueue();
        testWhiteBox();
        testUnboundedAndBounded();
        testTakeBlocksWhenEmpty();
        testPutBlocksWhenFull();
        testDrainTo();
        testNullRejection();
        testBlackBox();
        testSixMethodTwoStateTable();
        testConstructorIaeMessage();
        testUnboundedCapacityGrowth();
        testDrainToEdges();
        testTimedOfferWaitsWhenFull();
        testInterruptedTake();
        testInterruptStatusAtEntry();
        testInterruptedPut();
        testIteratorRemoveAndWeakConsistency();
        testSerializationRoundTrip();
        testNullRejectionExtended();
        testEqualsIdentitySemantics();
        testToArrayExactContract();
        testContainsRemoveMiddle();
        testAddAllOverflowPartial();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
