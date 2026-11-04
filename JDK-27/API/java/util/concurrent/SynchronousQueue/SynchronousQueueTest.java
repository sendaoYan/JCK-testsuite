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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SynchronousQueueTest {
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

    // ---- Part 3: blocking-queue contract deepening ----
    static void testCapacityZeroInvariantAfterHandoff() throws Exception {
        final SynchronousQueue<Integer> q = new SynchronousQueue<>();
        final CountDownLatch entered = new CountDownLatch(1), done = new CountDownLatch(1);
        final AtomicInteger got = new AtomicInteger(-1);
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
        q.put(5);
        awaitBounded(done, "handoff completion");
        c.join(5000);
        check(got.get() == 5, "handoff delivered the value");
        check(q.size() == 0 && q.isEmpty() && q.remainingCapacity() == 0,
                "capacity-0 invariant restored after handoff");
        check(!q.contains(5) && q.peek() == null, "handoff leaves no residue");
    }

    static void testAddFullMessage() {
        SynchronousQueue<Integer> q = new SynchronousQueue<>();
        IllegalStateException ise = expectThrowsMsg(() -> q.add(1),
                IllegalStateException.class, "add ISE");
        check("Queue full".equals(ise.getMessage()), "add message exactly 'Queue full'");
        check(q.isEmpty(), "queue still empty after failed add");
        check(!q.addAll(new ArrayList<Integer>()), "addAll(empty) returns false");
        IllegalStateException iseAll = expectThrowsMsg(() -> q.addAll(Arrays.asList(1, 2)),
                IllegalStateException.class, "addAll(non-empty) ISE");
        check("Queue full".equals(iseAll.getMessage()), "addAll message exactly 'Queue full'");
        check(q.isEmpty(), "queue still empty after failed addAll");
    }

    static void testEmptyStateTable() {
        SynchronousQueue<Integer> q = new SynchronousQueue<>();
        check(q.peek() == null, "empty-state: peek null");
        check(q.poll() == null, "empty-state: poll null");
        expectThrows(() -> q.element(), NoSuchElementException.class, "empty-state: element NSEE");
        expectThrows(() -> q.remove(), NoSuchElementException.class, "empty-state: remove() NSEE");
        check(q.isEmpty() && q.size() == 0, "empty-state: size 0");
    }

    static void testTimedOfferPollWaits() throws Exception {
        SynchronousQueue<Integer> q = new SynchronousQueue<>();
        long t0 = System.nanoTime();
        check(!q.offer(1, 200, TimeUnit.MILLISECONDS), "timed offer with no consumer waits then false");
        check(System.nanoTime() - t0 >= 150_000_000L, "timed offer waited ~200ms");
        t0 = System.nanoTime();
        check(q.poll(200, TimeUnit.MILLISECONDS) == null, "timed poll with no producer waits then null");
        check(System.nanoTime() - t0 >= 150_000_000L, "timed poll waited ~200ms");
    }

    static void testOfferWithWaitingConsumer() throws Exception {
        final SynchronousQueue<Integer> q = new SynchronousQueue<>();
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
        boolean offered = false;
        for (int i = 0; i < 2_000_000 && !offered; i++) {
            offered = q.offer(99); // succeeds once consumer is parked in take()
            if (!offered) Thread.yield();
        }
        check(offered, "untimed offer succeeds once a consumer is waiting");
        awaitBounded(done, "consumer completion");
        c.join(5000);
        check(got.get() == 99, "waiting consumer received the offered element");
        check(q.isEmpty(), "queue empty after rendezvous");
    }

    static void testTimedOfferWithWaitingConsumer() throws Exception {
        final SynchronousQueue<Integer> q = new SynchronousQueue<>();
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
        check(q.offer(7, 5, TimeUnit.SECONDS), "timed offer succeeds once a consumer is waiting");
        awaitBounded(done, "consumer completion");
        c.join(5000);
        check(got.get() == 7, "waiting consumer received the timed-offer element");
    }

    static void testFairHandoff() throws Exception {
        final SynchronousQueue<Integer> q = new SynchronousQueue<>(true);
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
        awaitBounded(entered, "fair consumer entry");
        q.put(3); // fair rendezvous
        awaitBounded(done, "fair handoff completion");
        c.join(5000);
        check(got.get() == 3, "fair queue hands off put->take");
        check(!q.offer(4) && q.poll() == null, "fair queue still has zero capacity");
    }

    static void testInterruptedTake() throws Exception {
        final SynchronousQueue<Integer> q = new SynchronousQueue<>();
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
        final SynchronousQueue<Integer> q = new SynchronousQueue<>();
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean gotIE = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            Thread.currentThread().interrupt();
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
        final SynchronousQueue<Integer> q = new SynchronousQueue<>();
        final CountDownLatch entered = new CountDownLatch(1), done = new CountDownLatch(1);
        final AtomicBoolean gotIE = new AtomicBoolean(false);
        Thread t = startDaemon(() -> {
            entered.countDown();
            try {
                q.put(1);
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
        check(q.isEmpty() && q.size() == 0, "queue unchanged after interrupted put");
        check(!t.isAlive(), "interrupted put thread terminated");
    }

    static void testNullRejectionExtended() {
        final SynchronousQueue<Integer> q = new SynchronousQueue<>();
        expectThrows(() -> q.add(null), NullPointerException.class, "add(null)");
        expectThrows(() -> q.offer(null), NullPointerException.class, "offer(null)");
        expectThrowsThrowing(NullPointerException.class, () -> q.put(null), "put(null)");
        expectThrowsThrowing(NullPointerException.class,
                () -> q.offer(null, 1, TimeUnit.SECONDS), "offer(null,timeout)");
        expectThrows(() -> q.drainTo(null), NullPointerException.class, "drainTo(null)");
        check(!q.contains(null), "contains(null) false");
        check(!q.remove(null), "remove(null) false");
    }

    static void testDrainToEdges() {
        SynchronousQueue<Integer> q = new SynchronousQueue<>();
        IllegalArgumentException iae = expectThrowsMsg(() -> q.drainTo(q),
                IllegalArgumentException.class, "drainTo(self) IAE");
        check(iae.getMessage() == null, "drainTo(self) IAE has null message");
        List<Integer> sink = new ArrayList<>();
        check(q.drainTo(sink, 0) == 0 && sink.isEmpty(), "drainTo(max=0) returns 0, mutates nothing");
    }

    static void testSerializationRoundTrip() throws Exception {
        SynchronousQueue<Integer> q = new SynchronousQueue<>();
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
        SynchronousQueue<Integer> rt = (SynchronousQueue<Integer>) back;
        check(rt.isEmpty() && rt.size() == 0 && rt.remainingCapacity() == 0,
                "round-tripped queue keeps capacity-0 invariants");
        check(!rt.offer(1) && rt.poll() == null && rt.peek() == null,
                "round-tripped queue still does not buffer");
    }

    static void testEqualsIdentitySemantics() {
        SynchronousQueue<Integer> a = new SynchronousQueue<>();
        SynchronousQueue<Integer> b = new SynchronousQueue<>();
        check(a.equals(a), "equals(self) true");
        check(!a.equals(b), "identity equals (distinct empty instances not equal)");
        check(!a.equals(null), "equals(null) false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testNoCapacity();
        testHandoff();
        testPutBlocksUntilTake();
        testImmediateTimeout();
        testFairConstructor();
        testBlackBox();
        testCapacityZeroInvariantAfterHandoff();
        testAddFullMessage();
        testEmptyStateTable();
        testTimedOfferPollWaits();
        testOfferWithWaitingConsumer();
        testTimedOfferWithWaitingConsumer();
        testFairHandoff();
        testInterruptedTake();
        testInterruptStatusAtEntry();
        testInterruptedPut();
        testNullRejectionExtended();
        testDrainToEdges();
        testSerializationRoundTrip();
        testEqualsIdentitySemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
