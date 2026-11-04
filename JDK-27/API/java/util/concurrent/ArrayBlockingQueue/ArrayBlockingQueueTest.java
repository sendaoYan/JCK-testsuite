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

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
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
        expectThrowsMsg(NullPointerException.class, () -> q.put(null), "put(null)");
        expectThrowsMsg(NullPointerException.class, () -> q.offer(null, 1, TimeUnit.SECONDS), "offer(null,timeout)");
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

        // ---- 边界值/等价类/等价性（本棒补测） ----
        // 容量 0 边界（非法）
        expectThrows(IllegalArgumentException.class, () -> new ArrayBlockingQueue<>(0),
                "capacity 0 constructor");
        expectThrows(IllegalArgumentException.class, () -> new ArrayBlockingQueue<>(-1),
                "capacity negative constructor");

        // 空队列查询边界
        ArrayBlockingQueue<String> empty = new ArrayBlockingQueue<>(2);
        expectThrows(NoSuchElementException.class, () -> empty.element(), "element() empty");
        expectThrows(NoSuchElementException.class, () -> empty.remove(), "remove() empty");
        check(empty.poll() == null && empty.peek() == null, "poll/peek empty null");

        // 满队列 add 抛异常（offer 语义决定）
        ArrayBlockingQueue<String> full = new ArrayBlockingQueue<>(2);
        full.add("a");
        full.add("b");
        expectThrows(IllegalStateException.class, () -> full.add("c"), "add() full");
        check(full.size() == 2, "size unchanged after failed add");

        // 满队列 offer(timeout=0) 立即失败
        check(!full.offer("c", 0, TimeUnit.NANOSECONDS), "offer(timeout 0) full false");
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static <T extends Throwable> T expectThrowsMsg(Class<T> type, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return type.cast(t); }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                    + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static void awaitBounded(CountDownLatch latch, String what) throws InterruptedException {
        if (!latch.await(5, TimeUnit.SECONDS))
            throw new AssertionError("timeout (5s) waiting for: " + what);
    }

    // ---- Part 3: blocking-queue contract deepening ----
    static void testSixMethodTwoStateTable() {
        ArrayBlockingQueue<String> q = new ArrayBlockingQueue<>(2);
        // empty state
        check(q.isEmpty() && q.size() == 0, "two-state: initially empty");
        expectThrows(NoSuchElementException.class, () -> q.remove(), "two-state: remove() empty NSEE");
        expectThrows(NoSuchElementException.class, () -> q.element(), "two-state: element() empty NSEE");
        check(q.poll() == null, "two-state: poll() empty null");
        check(q.peek() == null, "two-state: peek() empty null");
        check(q.add("a") && q.add("b"), "two-state: add succeeds while space remains");
        // full state
        IllegalStateException ise = expectThrowsMsg(IllegalStateException.class,
                () -> q.add("c"), "two-state: add() full ISE");
        check("Queue full".equals(ise.getMessage()), "two-state: add() full message exactly 'Queue full'");
        check(!q.offer("c"), "two-state: offer() full false");
        check(q.size() == 2, "two-state: failed insert leaves size unchanged");
        check("a".equals(q.element()) && "a".equals(q.peek()), "two-state: element/peek full return head");
        check("a".equals(q.remove()) && "b".equals(q.remove()), "two-state: remove() drains FIFO");
    }

    static void testConstructorIaeMessage() {
        IllegalArgumentException e0 = expectThrowsMsg(IllegalArgumentException.class,
                () -> new ArrayBlockingQueue<String>(0), "ctor capacity 0 IAE");
        check(e0.getMessage() == null, "ctor capacity 0 IAE has null message");
        IllegalArgumentException en = expectThrowsMsg(IllegalArgumentException.class,
                () -> new ArrayBlockingQueue<String>(-1), "ctor capacity negative IAE");
        check(en.getMessage() == null, "ctor negative IAE has null message");
        ArrayBlockingQueue<String> one = new ArrayBlockingQueue<>(1);
        check(one.remainingCapacity() == 1 && one.isEmpty() && one.offer("x"), "capacity 1 constructor usable");
    }

    static void testDrainToEdges() {
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(4);
        q.add(1);
        q.add(2);
        List<Integer> sink = new ArrayList<>();
        check(q.drainTo(sink, 0) == 0, "drainTo(max=0) returns 0");
        check(sink.isEmpty() && q.size() == 2, "drainTo(max=0) mutates nothing");
        IllegalArgumentException iae = expectThrowsMsg(IllegalArgumentException.class,
                () -> q.drainTo(q), "drainTo(self) IAE");
        check(iae.getMessage() == null, "drainTo(self) IAE has null message");
        check(q.size() == 2, "drainTo(self) leaves queue unchanged");
        check(q.drainTo(sink) == 2 && sink.equals(Arrays.asList(1, 2)) && q.isEmpty(),
                "drainTo full drain: FIFO order + source cleared");
        q.add(3);
        q.add(4);
        q.add(5);
        List<Integer> part = new ArrayList<>();
        check(q.drainTo(part, 2) == 2 && part.equals(Arrays.asList(3, 4)),
                "drainTo(max) truncates at max in FIFO order");
        check(q.size() == 1 && q.peek() == 5, "drainTo(max) leaves remainder");
    }

    static void testTimedOfferWaitsWhenFull() throws Exception {
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(1);
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
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(1);
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
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(1);
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
        check(q.isEmpty(), "queue untouched by interrupted take");
    }

    static void testInterruptedPut() throws Exception {
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(1);
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
        check(!t.isAlive(), "interrupted put thread terminated");
    }

    static void testIteratorWeakConsistency() {
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(4);
        q.add(1);
        q.add(2);
        q.add(3);
        java.util.Iterator<Integer> it = q.iterator();
        check(it.hasNext() && it.next() == 1, "iterator first element");
        check(q.remove(Integer.valueOf(3)), "structural removal during iteration");
        List<Integer> rest = new ArrayList<>();
        while (it.hasNext()) rest.add(it.next()); // must not throw CME
        check(rest.equals(Arrays.asList(2)), "weakly consistent iterator survives removal, no CME");
    }

    static void testSerializationRoundTrip() throws Exception {
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(3);
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
        ArrayBlockingQueue<Integer> rt = (ArrayBlockingQueue<Integer>) back;
        check(rt.size() == 2, "serialization round-trip preserves element count");
        check(rt.poll() == 1 && rt.poll() == 2 && rt.isEmpty(), "serialization round-trip preserves FIFO order");
        check(rt.remainingCapacity() == 3, "serialization round-trip preserves capacity");
        check(rt.offer(1) && rt.offer(2) && rt.offer(3), "round-tripped queue accepts capacity elements");
        check(!rt.offer(4), "round-tripped queue is still bounded by original capacity");
    }

    static void testEqualsIdentitySemantics() {
        ArrayBlockingQueue<Integer> a = new ArrayBlockingQueue<>(4);
        ArrayBlockingQueue<Integer> b = new ArrayBlockingQueue<>(4);
        check(a.add(1) && a.add(2) && b.add(1) && b.add(2), "setup: equal contents, distinct instances");
        check(a.equals(a), "equals(self) true");
        check(!a.equals(b), "no element-based equals override (identity semantics)");
        check(!a.equals(null), "equals(null) false");
        check(!a.equals(Arrays.asList(1, 2)), "queue not equal to List with same elements");
    }

    static void testToArrayExactContract() {
        ArrayBlockingQueue<String> q = new ArrayBlockingQueue<>(4);
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
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(4);
        q.add(1);
        q.add(2);
        q.add(3);
        check(q.contains(2), "contains middle element");
        check(q.remove(Integer.valueOf(2)), "remove middle element");
        check(!q.contains(2) && q.size() == 2, "middle element gone");
        check(Arrays.equals(q.toArray(), new Object[]{1, 3}), "order preserved after middle removal");
    }

    static void testAddAllOverflowPartial() {
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(2);
        IllegalStateException ise = expectThrowsMsg(IllegalStateException.class,
                () -> q.addAll(Arrays.asList(1, 2, 3)), "addAll overflow ISE");
        check("Queue full".equals(ise.getMessage()), "addAll overflow message 'Queue full'");
        check(q.size() == 2, "addAll overflow retains already-inserted prefix");
        check(q.poll() == 1 && q.poll() == 2 && q.isEmpty(), "retained prefix in order");
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
        testSixMethodTwoStateTable();
        testConstructorIaeMessage();
        testDrainToEdges();
        testTimedOfferWaitsWhenFull();
        testInterruptedTake();
        testInterruptStatusAtEntry();
        testInterruptedPut();
        testIteratorWeakConsistency();
        testSerializationRoundTrip();
        testEqualsIdentitySemantics();
        testToArrayExactContract();
        testContainsRemoveMiddle();
        testAddAllOverflowPartial();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
