/*
 * @test
 * @summary java.util.concurrent.DelayQueue conformance: white-box + black-box
 *          (deterministic getDelay unit conversions, expired items poll in expiry order,
 *          unexpired items: poll null / peek returns heap head / drainTo skips, mixed
 *          expiry, take eventually returns on a real bounded delay, take pairing with
 *          producer, interrupted take, null rejection, non-Delayed ClassCastException,
 *          not-Serializable contract, put never blocks, empty-state table, drainTo
 *          edges, identity equals).
 * @run main DelayQueueTest
 */

import java.lang.reflect.Modifier;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DelayQueueTest {
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

    // preserved original item class
    static class Item implements Delayed {
        final long expireNanos;

        Item(long delayNanos) {
            this.expireNanos = System.nanoTime() + delayNanos;
        }

        public long getDelay(TimeUnit unit) {
            return unit.convert(expireNanos - System.nanoTime(), TimeUnit.NANOSECONDS);
        }

        public int compareTo(Delayed o) {
            return Long.compare(expireNanos, ((Item) o).expireNanos);
        }
    }

    // Deterministic item: fixed delay value independent of wall clock.
    // Negative nanos => already expired; large positive => not expired within test life.
    static final class Fixed implements Delayed {
        final long nanos;
        final int id;
        Fixed(long nanos, int id) { this.nanos = nanos; this.id = id; }
        public long getDelay(TimeUnit unit) { return unit.convert(nanos, TimeUnit.NANOSECONDS); }
        public int compareTo(Delayed o) { return Long.compare(nanos, ((Fixed) o).nanos); }
        public String toString() { return "Fixed#" + id; }
    }

    // Real-time item used only where a bounded real delay is the point of the test.
    static final class NanoItem implements Delayed {
        final long trigger;
        final int id;
        NanoItem(long delayNanos, int id) {
            this.trigger = System.nanoTime() + delayNanos;
            this.id = id;
        }
        public long getDelay(TimeUnit unit) {
            return unit.convert(trigger - System.nanoTime(), TimeUnit.NANOSECONDS);
        }
        public int compareTo(Delayed o) { return Long.compare(trigger, ((NanoItem) o).trigger); }
    }

    // Item whose compareTo is reversed w.r.t. its deadline: proves DelayQueue orders
    // purely by the element's compareTo (natural ordering of Delayed), not by deadline.
    static final class ReverseItem implements Delayed {
        final long nanos;
        final int id;
        ReverseItem(long nanos, int id) { this.nanos = nanos; this.id = id; }
        public long getDelay(TimeUnit unit) { return unit.convert(nanos, TimeUnit.NANOSECONDS); }
        public int compareTo(Delayed o) { return Long.compare(((ReverseItem) o).nanos, nanos); }
        public String toString() { return "Reverse#" + id; }
    }

    // preserved original smoke test
    static void testDelayQueue() {
        DelayQueue<Item> q = new DelayQueue<>();
        q.add(new Item(-1_000_000_000L)); // already expired (1s ago)
        check(q.poll() != null, "poll returns an expired item");
        check(q.isEmpty(), "queue empty after poll");
        q.add(new Item(60_000_000_000L)); // 60s in the future
        check(q.poll() == null, "poll returns null for an unexpired item");
        check(q.size() == 1, "size 1 after unexpired add");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.DelayQueue");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractQueue.class, "extends AbstractQueue");
        check(BlockingQueue.class.isAssignableFrom(c), "implements BlockingQueue");
        check(!java.io.Serializable.class.isAssignableFrom(c),
                "DelayQueue is NOT Serializable (unlike the other blocking queues)");
        check(c.getConstructor() != null && c.getConstructor(java.util.Collection.class) != null,
              "constructors ()/(Collection)");
        check(hasMethod(c, "add", Object.class) && hasMethod(c, "offer", Object.class), "add/offer");
        check(hasMethod(c, "put", Object.class) && hasMethod(c, "take"), "put/take");
        check(hasMethod(c, "poll") && hasMethod(c, "peek"), "poll/peek");
        check(hasMethod(c, "poll", long.class, TimeUnit.class), "poll(long,TimeUnit)");
        check(hasMethod(c, "size") && hasMethod(c, "remainingCapacity"), "size/remainingCapacity");
        check(hasMethod(c, "drainTo", java.util.Collection.class), "drainTo");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testExpiredImmediate() {
        DelayQueue<NanoItem> q = new DelayQueue<>();
        check(q.isEmpty() && q.size() == 0, "empty initially");
        check(q.remainingCapacity() == Integer.MAX_VALUE, "unbounded remainingCapacity");
        // already-expired item (1 second in the past) -> poll/take return immediately.
        NanoItem expired = new NanoItem(-1_000_000_000L, 1);
        q.add(expired);
        check(q.peek() == expired, "peek returns expired item without removing");
        check(q.size() == 1, "size after add");
        check(q.poll() == expired, "poll returns expired item immediately");
        check(q.isEmpty(), "empty after poll");
    }

    static void testUnexpiredNull() {
        DelayQueue<NanoItem> q = new DelayQueue<>();
        q.add(new NanoItem(1_000_000_000_000L, 2)); // ~1000s in the future
        check(q.poll() == null, "poll null for unexpired item");
        check(q.size() == 1, "unexpired item stays in queue");
    }

    static void testPollTimeoutImmediate() throws Exception {
        DelayQueue<NanoItem> q = new DelayQueue<>();
        NanoItem expired = new NanoItem(-1_000_000_000L, 3);
        q.add(expired);
        check(q.poll(1, TimeUnit.NANOSECONDS) == expired, "poll(timeout) returns expired immediately");

        q.add(new NanoItem(1_000_000_000_000L, 4)); // unexpired
        check(q.poll(1, TimeUnit.NANOSECONDS) == null, "poll(timeout) null for unexpired");
    }

    static void testTakeImmediateForExpired() throws Exception {
        DelayQueue<NanoItem> q = new DelayQueue<>();
        NanoItem expired = new NanoItem(-1_000_000_000L, 5);
        q.add(expired);
        check(q.take() == expired, "take returns already-expired item immediately");
    }

    static void testDrainToExpiredOnly() {
        DelayQueue<NanoItem> q = new DelayQueue<>();
        NanoItem e1 = new NanoItem(-1_000_000_000L, 6);
        NanoItem e2 = new NanoItem(-1_000_000_000L, 7);
        q.add(e1);
        q.add(e2);
        q.add(new NanoItem(1_000_000_000_000L, 8)); // unexpired
        List<NanoItem> drained = new ArrayList<>();
        check(q.drainTo(drained) == 2, "drainTo drains only expired items");
        check(drained.size() == 2, "drained size");
        check(q.size() == 1, "unexpired item remains");
    }

    static void testNullRejection() {
        DelayQueue<NanoItem> q = new DelayQueue<>();
        expectThrows(() -> q.add(null), NullPointerException.class, "add(null)");
        expectThrows(() -> q.offer(null), NullPointerException.class, "offer(null)");
    }

    static void testBlackBox() {
        // constructor(Collection) with distinct deadlines (deterministic head order)
        NanoItem e1 = new NanoItem(-1_000_000_000L, 10); // later expired
        NanoItem e2 = new NanoItem(-2_000_000_000L, 11); // earlier expired (head)
        List<NanoItem> seed = new ArrayList<>();
        seed.add(e1);
        seed.add(e2);
        DelayQueue<NanoItem> q = new DelayQueue<>(seed);
        check(q.size() == 2, "constructor(Collection) copies");

        // iterator enumerates all
        java.util.Iterator<NanoItem> it = q.iterator();
        int count = 0;
        while (it.hasNext()) { it.next(); count++; }
        check(count == 2, "iterator enumerates all");

        // toArray / toArray(T[])
        Object[] arr = q.toArray();
        check(arr.length == 2, "toArray()");
        NanoItem[] typed = q.toArray(new NanoItem[0]);
        check(typed.length == 2, "toArray(T[])");

        // remove() (head) / remove(Object)
        check(q.remove() == e2, "remove() returns earliest-deadline head");
        check(q.remove(e1), "remove(Object) true");
        check(!q.remove(e1), "remove(Object) missing false");
        check(q.isEmpty(), "empty after removes");

        // clear
        q.add(new NanoItem(-1_000_000_000L, 12));
        q.add(new NanoItem(-1_000_000_000L, 13));
        q.clear();
        check(q.isEmpty(), "clear empties");
    }

    // ---- Part 3: blocking-queue contract deepening ----
    static void testGetDelayConversionTable() {
        Fixed positive = new Fixed(1_500_000_001L, 1);
        check(positive.getDelay(TimeUnit.NANOSECONDS) == 1_500_000_001L, "getDelay(NANOSECONDS) exact");
        check(positive.getDelay(TimeUnit.MICROSECONDS) == 1_500_000L,
                "getDelay(MICROSECONDS) truncates nanos->micros");
        check(positive.getDelay(TimeUnit.MILLISECONDS) == 1_500L, "getDelay(MILLISECONDS) exact truncation");
        check(positive.getDelay(TimeUnit.SECONDS) == 1L, "getDelay(SECONDS) truncates 1.5s -> 1");
        Fixed negative = new Fixed(-2_500_000_000L, 2);
        check(negative.getDelay(TimeUnit.NANOSECONDS) == -2_500_000_000L, "negative delay ns exact");
        check(negative.getDelay(TimeUnit.MILLISECONDS) == -2_500L, "negative delay ms truncates toward zero");
        check(negative.getDelay(TimeUnit.SECONDS) == -2L, "negative -2.5s truncates toward zero -> -2");
    }

    static void testExpiryOrderExact() {
        DelayQueue<Fixed> q = new DelayQueue<>();
        Fixed late = new Fixed(-1_000_000_000L, 1);
        Fixed mid = new Fixed(-5_000_000_000L, 2);
        Fixed early = new Fixed(-9_000_000_000L, 3);
        q.add(late);
        q.add(mid);
        q.add(early);
        check(q.size() == 3, "expiry-order: size 3");
        check(q.peek() == early, "peek returns earliest-deadline item");
        check(q.element() == early, "element returns earliest-deadline item");
        check(q.poll() == early, "poll returns earliest expired");
        check(q.poll() == mid && q.poll() == late, "poll drains in deadline order");
        check(q.isEmpty(), "expiry-order: empty after");
    }

    static void testUnexpiredSemantics() {
        DelayQueue<Fixed> q = new DelayQueue<>();
        Fixed future = new Fixed(10_000_000_000L, 9); // 10s away, never expires during test
        q.add(future);
        check(q.poll() == null, "unexpired: poll null");
        check(q.peek() == future, "unexpired: peek returns heap head (earliest deadline)");
        check(q.element() == future, "unexpired: element returns head without throwing");
        check(q.size() == 1, "unexpired: size counts the item");
        check(q.contains(future), "unexpired: contains true");
        int seen = 0;
        for (Fixed f : q) seen++;
        check(seen == 1, "unexpired: iterator includes the item");
        List<Fixed> drained = new ArrayList<>();
        check(q.drainTo(drained) == 0 && drained.isEmpty(), "unexpired: drainTo removes nothing");
        check(q.size() == 1, "unexpired: item remains after drainTo");
        check(q.remainingCapacity() == Integer.MAX_VALUE, "unexpired: unbounded capacity");
    }

    static void testMixedExpiry() {
        DelayQueue<Fixed> q = new DelayQueue<>();
        Fixed e1 = new Fixed(-9_000_000_000L, 1);
        Fixed e2 = new Fixed(-5_000_000_000L, 2);
        Fixed future = new Fixed(9_000_000_000L, 3);
        q.add(e2);
        q.add(future);
        q.add(e1);
        List<Fixed> drained = new ArrayList<>();
        check(q.drainTo(drained) == 2, "mixed: drainTo takes exactly the expired items");
        check(drained.equals(Arrays.asList(e1, e2)), "mixed: drainTo order follows deadlines");
        check(q.size() == 1 && q.peek() == future, "mixed: unexpired item remains");
        q.add(e1);
        q.add(e2);
        List<Fixed> one = new ArrayList<>();
        check(q.drainTo(one, 1) == 1 && one.equals(Arrays.asList(e1)),
                "mixed: drainTo(max=1) takes only the earliest expired");
        List<Fixed> zero = new ArrayList<>();
        check(q.drainTo(zero, 0) == 0 && zero.isEmpty(), "drainTo(max=0) returns 0, mutates nothing");
    }

    static void testCustomOrderingViaCompareTo() {
        DelayQueue<ReverseItem> q = new DelayQueue<>();
        ReverseItem a = new ReverseItem(-1_000_000_000L, 1); // later deadline
        ReverseItem b = new ReverseItem(-9_000_000_000L, 2); // earlier deadline
        q.add(a);
        q.add(b);
        // reversed compareTo: the later-deadline item is "smaller"
        check(q.poll() == a, "queue follows element compareTo, not deadline direction");
        check(q.poll() == b, "second poll returns remaining item");
    }

    static void testTakeEventuallyOnRealDelay() throws Exception {
        final DelayQueue<NanoItem> q = new DelayQueue<>();
        q.add(new NanoItem(250_000_000L, 7)); // expires 250ms from now
        final AtomicReference<NanoItem> got = new AtomicReference<>();
        final CountDownLatch done = new CountDownLatch(1);
        Thread t = startDaemon(() -> {
            try {
                got.set(q.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });
        // bounded wait (3s) tolerates scheduler delay; asserts eventual availability, not timing
        if (!done.await(3, TimeUnit.SECONDS))
            throw new AssertionError("take did not return within 3s of the 250ms deadline");
        t.join(5000);
        check(got.get() != null && got.get().id == 7, "take returns once the delay elapses");
        check(q.isEmpty(), "queue empty after delayed take");
    }

    static void testTakePairingWithProducer() throws Exception {
        final DelayQueue<Fixed> q = new DelayQueue<>();
        final AtomicReference<Fixed> got = new AtomicReference<>();
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
        q.add(new Fixed(-1_000_000_000L, 5)); // expired item unblocks the blocked take
        awaitBounded(done, "consumer completion");
        c.join(5000);
        check(got.get() != null && got.get().id == 5, "blocked take receives the expired item");
    }

    static void testInterruptedTake() throws Exception {
        final DelayQueue<Fixed> q = new DelayQueue<>();
        q.add(new Fixed(10_000_000_000L, 1)); // unexpired: take must block
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
        check(q.size() == 1, "unexpired item still queued after interrupted take");
    }

    static void testInterruptStatusAtEntry() throws Exception {
        final DelayQueue<Fixed> q = new DelayQueue<>();
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

    static void testPollTimeoutUnexpiredWaits() throws Exception {
        DelayQueue<Fixed> q = new DelayQueue<>();
        q.add(new Fixed(10_000_000_000L, 1));
        long t0 = System.nanoTime();
        check(q.poll(150, TimeUnit.MILLISECONDS) == null,
                "timed poll waits for expiry then returns null");
        check(System.nanoTime() - t0 >= 100_000_000L, "timed poll waited ~150ms, not immediate");
    }

    static void testPutNeverBlocks() throws Exception {
        DelayQueue<Fixed> q = new DelayQueue<>();
        q.put(new Fixed(10_000_000_000L, 1));
        q.put(new Fixed(9_000_000_000L, 2));
        check(q.size() == 2, "put adds unexpired items immediately (never blocks)");
        check(q.peek().id == 2, "heap head is the earliest deadline");
    }

    static void testNullAndNonDelayed() {
        final DelayQueue<Fixed> q = new DelayQueue<>();
        expectThrows(() -> q.add(null), NullPointerException.class, "add(null)");
        expectThrows(() -> q.offer(null), NullPointerException.class, "offer(null)");
        expectThrows(() -> q.put(null), NullPointerException.class, "put(null)");
        expectThrows(() -> { q.offer(null, 1, TimeUnit.SECONDS); }, NullPointerException.class,
                "offer(null,timeout)");
        expectThrows(() -> q.drainTo(null), NullPointerException.class, "drainTo(null)");
        check(!q.contains(null), "contains(null) false");
        check(!q.remove(null), "remove(null) false");
        @SuppressWarnings("rawtypes")
        final DelayQueue raw = new DelayQueue();
        expectThrows(() -> raw.offer((Delayed) new Object()), ClassCastException.class,
                "offer(non-Delayed) CCE");
        expectThrows(() -> {
            Object o = new DelayQueue(Arrays.asList(new Object()));
        }, ClassCastException.class, "constructor(non-Delayed collection) CCE");
    }

    static void testNotSerializable() throws Exception {
        check(!java.io.Serializable.class.isAssignableFrom(DelayQueue.class),
                "DelayQueue does not implement Serializable");
        DelayQueue<Fixed> q = new DelayQueue<>();
        q.add(new Fixed(-1_000_000_000L, 1));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(q);
            check(false, "serializing a DelayQueue must fail");
        } catch (java.io.NotSerializableException expected) {
            passed++;
        }
    }

    static void testEmptyStateTable() {
        DelayQueue<Fixed> q = new DelayQueue<>();
        check(q.isEmpty() && q.size() == 0, "empty-state: size 0");
        check(q.peek() == null, "empty-state: peek null");
        check(q.poll() == null, "empty-state: poll null");
        expectThrows(() -> q.element(), NoSuchElementException.class, "empty-state: element NSEE");
        expectThrows(() -> q.remove(), NoSuchElementException.class, "empty-state: remove() NSEE");
        check(q.remainingCapacity() == Integer.MAX_VALUE, "empty-state: unbounded");
    }

    static void testDrainToEdges() {
        DelayQueue<Fixed> q = new DelayQueue<>();
        q.add(new Fixed(-5_000_000_000L, 1));
        IllegalArgumentException iae = expectThrowsMsg(() -> q.drainTo(q),
                IllegalArgumentException.class, "drainTo(self) IAE");
        check(iae.getMessage() == null, "drainTo(self) IAE has null message");
        check(q.size() == 1, "drainTo(self) leaves queue unchanged");
        List<Fixed> sink = new ArrayList<>();
        check(q.drainTo(sink) == 1 && sink.size() == 1 && q.isEmpty(),
                "drainTo expired item with source cleared");
    }

    static void testEqualsIdentitySemantics() {
        DelayQueue<Fixed> a = new DelayQueue<>();
        DelayQueue<Fixed> b = new DelayQueue<>();
        check(a.add(new Fixed(-1L, 1)) && b.add(new Fixed(-1L, 1)), "setup: equal contents");
        check(a.equals(a), "equals(self) true");
        check(!a.equals(b), "identity equals (no element-based override)");
        check(!a.equals(null), "equals(null) false");
    }

    public static void main(String[] args) throws Exception {
        testDelayQueue();
        testWhiteBox();
        testExpiredImmediate();
        testUnexpiredNull();
        testPollTimeoutImmediate();
        testTakeImmediateForExpired();
        testDrainToExpiredOnly();
        testNullRejection();
        testBlackBox();
        testGetDelayConversionTable();
        testExpiryOrderExact();
        testUnexpiredSemantics();
        testMixedExpiry();
        testCustomOrderingViaCompareTo();
        testTakeEventuallyOnRealDelay();
        testTakePairingWithProducer();
        testInterruptedTake();
        testInterruptStatusAtEntry();
        testPollTimeoutUnexpiredWaits();
        testPutNeverBlocks();
        testNullAndNonDelayed();
        testNotSerializable();
        testEmptyStateTable();
        testDrainToEdges();
        testEqualsIdentitySemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
