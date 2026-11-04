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
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

    interface ThrowingRunnable { void run() throws Throwable; }

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

    static <T extends Throwable> T expectThrowsThrowing(Class<T> type, ThrowingRunnable r, String msg) {
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

    static void testBlackBox() {
        // constructor(Collection)
        LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>(Arrays.asList(1, 2, 3));
        check(q.size() == 3, "constructor(Collection) copies");
        check(q.remainingCapacity() == Integer.MAX_VALUE, "remainingCapacity unbounded");
        check(q.contains(2) && !q.contains(99), "contains");

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
        LinkedTransferQueue<Integer> spq = new LinkedTransferQueue<>(Arrays.asList(1, 2, 3));
        java.util.Spliterator<Integer> sp = spq.spliterator();
        check(sp.estimateSize() == Long.MAX_VALUE, "spliterator estimateSize Long.MAX_VALUE");
        List<Integer> viaSp = new ArrayList<>();
        sp.forEachRemaining(viaSp::add);
        check(viaSp.equals(Arrays.asList(1, 2, 3)), "spliterator order");

        // clear
        spq.clear();
        check(spq.isEmpty(), "clear empties");

        // toString
        LinkedTransferQueue<Integer> ts = new LinkedTransferQueue<>(Arrays.asList(1, 2));
        check(ts.toString() != null && ts.toString().contains("1"), "toString non-null");
    }

    // ---- Part 3: blocking-queue contract deepening ----
    static void testEmptyStateTable() {
        LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        expectThrows(() -> q.element(), NoSuchElementException.class, "empty-state: element NSEE");
        expectThrows(() -> q.remove(), NoSuchElementException.class, "empty-state: remove() NSEE");
        check(q.poll() == null, "empty-state: poll null");
        check(q.peek() == null, "empty-state: peek null");
        check(q.isEmpty() && q.size() == 0, "empty-state: size 0");
    }

    static void testFifoStrictTen() {
        LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        boolean all = true;
        for (int i = 0; i < 10; i++) all &= q.offer(i);
        check(all, "unbounded offer of 10 elements never fails");
        boolean fifo = true;
        for (int i = 0; i < 10; i++) fifo &= (q.poll() == i);
        check(fifo, "10 elements drain in strict FIFO order");
        check(q.isEmpty(), "empty after draining 10");
    }

    static void testTimedTryTransferNoConsumer() throws Exception {
        LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        long t0 = System.nanoTime();
        check(!q.tryTransfer(7, 200, TimeUnit.MILLISECONDS),
                "timed tryTransfer waits then returns false with no consumer");
        check(System.nanoTime() - t0 >= 150_000_000L, "timed tryTransfer waited ~200ms");
        check(q.size() == 0 && q.isEmpty(), "failed timed tryTransfer does not enqueue");
    }

    static void testTryTransferWithWaitingConsumer() throws Exception {
        final LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        final AtomicInteger got = new AtomicInteger(-1);
        final CountDownLatch entered = new CountDownLatch(1), done = new CountDownLatch(1);
        Thread c = startDaemon(() -> {
            entered.countDown();
            try {
                got.set(q.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });
        awaitBounded(entered, "consumer entry");
        int spins = 0;
        while (!q.hasWaitingConsumer()) {
            Thread.yield();
            if (++spins > 2_000_000)
                throw new AssertionError("consumer never registered as waiting");
        }
        check(q.hasWaitingConsumer(), "hasWaitingConsumer true while consumer blocked");
        check(q.getWaitingConsumerCount() == 1, "exactly one waiting consumer");
        check(q.tryTransfer(42), "tryTransfer true with waiting consumer");
        awaitBounded(done, "consumer completion");
        c.join(5000);
        check(got.get() == 42, "waiting consumer received transferred element");
        check(q.isEmpty(), "queue empty after successful tryTransfer");
        check(!q.hasWaitingConsumer() && q.getWaitingConsumerCount() == 0,
                "no waiting consumer after handoff");
    }

    static void testTimedTryTransferWithConsumer() throws Exception {
        final LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        final AtomicInteger got = new AtomicInteger(-1);
        final CountDownLatch entered = new CountDownLatch(1), done = new CountDownLatch(1);
        Thread c = startDaemon(() -> {
            entered.countDown();
            try {
                got.set(q.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });
        awaitBounded(entered, "consumer entry");
        check(q.tryTransfer(9, 5, TimeUnit.SECONDS), "timed tryTransfer true once consumer arrives");
        awaitBounded(done, "consumer completion");
        c.join(5000);
        check(got.get() == 9, "consumer received the timed tryTransfer element");
    }

    static void testTakeBlockedPairingWithProducer() throws Exception {
        final LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        final AtomicInteger got = new AtomicInteger(-1);
        final CountDownLatch entered = new CountDownLatch(1), done = new CountDownLatch(1);
        Thread c = startDaemon(() -> {
            entered.countDown();
            try {
                got.set(q.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });
        awaitBounded(entered, "consumer entry");
        q.put(9); // unblocks the blocked take
        awaitBounded(done, "consumer completion");
        c.join(5000);
        check(got.get() == 9, "blocked take receives element from put");
        check(q.isEmpty(), "queue empty after pairing");
    }

    static void testTimedPollEmptyWaits() throws Exception {
        LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        long t0 = System.nanoTime();
        check(q.poll(200, TimeUnit.MILLISECONDS) == null, "timed poll on empty waits then returns null");
        check(System.nanoTime() - t0 >= 150_000_000L, "timed poll waited ~200ms");
    }

    static void testInterruptedTake() throws Exception {
        final LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        final CountDownLatch entered = new CountDownLatch(1), done = new CountDownLatch(1);
        final AtomicBoolean gotIE = new AtomicBoolean(false);
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
        final LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean gotIE = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            Thread.currentThread().interrupt();
            try {
                q.transfer(1);
            } catch (InterruptedException e) {
                gotIE.set(true);
            } finally {
                done.countDown();
            }
        });
        awaitBounded(done, "pre-interrupted transfer completion");
        t.join(5000);
        check(gotIE.get(), "transfer with pre-set interrupt status throws InterruptedException");
        check(q.isEmpty(), "queue untouched by interrupted transfer");
    }

    static void testInterruptedTransfer() throws Exception {
        final LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        final CountDownLatch entered = new CountDownLatch(1), done = new CountDownLatch(1);
        final AtomicBoolean gotIE = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            entered.countDown();
            try {
                q.transfer(1);
            } catch (InterruptedException e) {
                gotIE.set(true);
            } finally {
                done.countDown();
            }
        });
        awaitBounded(entered, "transfer thread entry");
        t.interrupt();
        awaitBounded(done, "interrupted transfer completion");
        t.join(5000);
        check(gotIE.get(), "blocked transfer interrupted -> InterruptedException");
        check(q.isEmpty(), "queue untouched after interrupted transfer");
    }

    static void testNullRejectionExtended() {
        final LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        expectThrows(() -> q.add(null), NullPointerException.class, "add(null)");
        expectThrows(() -> q.offer(null), NullPointerException.class, "offer(null)");
        expectThrowsThrowing(NullPointerException.class, () -> q.put(null), "put(null)");
        expectThrowsThrowing(NullPointerException.class, () -> q.transfer(null), "transfer(null)");
        expectThrowsThrowing(NullPointerException.class, () -> q.tryTransfer(null), "tryTransfer(null)");
        expectThrowsThrowing(NullPointerException.class,
                () -> q.tryTransfer(null, 1, TimeUnit.SECONDS), "tryTransfer(null,timeout)");
        expectThrowsThrowing(NullPointerException.class,
                () -> q.offer(null, 1, TimeUnit.SECONDS), "offer(null,timeout)");
        expectThrows(() -> q.drainTo(null), NullPointerException.class, "drainTo(null)");
        check(!q.contains(null), "contains(null) false");
        check(!q.remove(null), "remove(null) false");
        check(q.isEmpty(), "queue unchanged after null rejections");
    }

    static void testIteratorRemoveAndWeakConsistency() {
        LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        q.add(1);
        q.add(2);
        q.add(3);
        java.util.Iterator<Integer> it = q.iterator();
        check(it.hasNext() && it.next() == 1, "iterator first element");
        it.remove();
        check(q.size() == 2 && q.peek() == 2, "iterator.remove removes current element");
        // weak consistency: fresh queue, first next() consumed, then structural removal
        LinkedTransferQueue<Integer> w = new LinkedTransferQueue<>();
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
        LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>(Arrays.asList(1, 2));
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
        LinkedTransferQueue<Integer> rt = (LinkedTransferQueue<Integer>) back;
        check(rt.size() == 2, "serialization round-trip preserves element count");
        check(rt.poll() == 1 && rt.poll() == 2 && rt.isEmpty(), "serialization round-trip preserves FIFO order");
        check(rt.remainingCapacity() == Integer.MAX_VALUE, "round-tripped queue still unbounded");
        check(rt.offer(3), "round-tripped queue still accepts elements");
    }

    static void testDrainToEdges() {
        LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
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

    static void testEqualsIdentitySemantics() {
        LinkedTransferQueue<Integer> a = new LinkedTransferQueue<>();
        LinkedTransferQueue<Integer> b = new LinkedTransferQueue<>();
        check(a.add(1) && a.add(2) && b.add(1) && b.add(2), "setup: equal contents, distinct instances");
        check(a.equals(a), "equals(self) true");
        check(!a.equals(b), "no element-based equals override (identity semantics)");
        check(!a.equals(null), "equals(null) false");
        check(!a.equals(Arrays.asList(1, 2)), "queue not equal to List with same elements");
    }

    static void testToArrayExactContract() {
        LinkedTransferQueue<String> q = new LinkedTransferQueue<>();
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
        LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>();
        q.add(1);
        q.add(2);
        q.add(3);
        check(q.contains(2), "contains middle element");
        check(q.remove(Integer.valueOf(2)), "remove middle element");
        check(!q.contains(2) && q.size() == 2, "middle element gone");
        check(Arrays.equals(q.toArray(), new Object[]{1, 3}), "order preserved after middle removal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testFifoOfferPoll();
        testTransferBlocksUntilTaken();
        testTryTransferImmediate();
        testWaitingConsumerIntrospection();
        testDrainToAndNull();
        testBlackBox();
        testEmptyStateTable();
        testFifoStrictTen();
        testTimedTryTransferNoConsumer();
        testTryTransferWithWaitingConsumer();
        testTimedTryTransferWithConsumer();
        testTakeBlockedPairingWithProducer();
        testTimedPollEmptyWaits();
        testInterruptedTake();
        testInterruptStatusAtEntry();
        testInterruptedTransfer();
        testNullRejectionExtended();
        testIteratorRemoveAndWeakConsistency();
        testSerializationRoundTrip();
        testDrainToEdges();
        testEqualsIdentitySemantics();
        testToArrayExactContract();
        testContainsRemoveMiddle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
