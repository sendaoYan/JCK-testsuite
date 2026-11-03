/*
 * @test
 * @summary java.util.concurrent.DelayQueue conformance: white-box + black-box
 *          (fixed-nanosecond-delay Delayed items, poll/take immediate for expired,
 *          poll(timeout) immediate paths, peek/drainTo/size/remainingCapacity, null
 *          rejection; deterministic, no wall-clock).
 * @run main DelayQueueTest
 */

import java.lang.reflect.Modifier;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

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

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // Fixed nanosecond-delay Delayed; deadline based on monotonic nanoTime, not wall clock.
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
        public int compareTo(Delayed o) {
            return Long.compare(trigger, ((NanoItem) o).trigger);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.DelayQueue");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractQueue.class, "extends AbstractQueue");
        check(BlockingQueue.class.isAssignableFrom(c), "implements BlockingQueue");
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testExpiredImmediate();
        testUnexpiredNull();
        testPollTimeoutImmediate();
        testTakeImmediateForExpired();
        testDrainToExpiredOnly();
        testNullRejection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
