/*
 * @test
 * @summary java.util.concurrent.DelayQueue API conformance: poll semantics by delay.
 * @run main DelayQueueTest
 */

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    static void testDelayQueue() {
        DelayQueue<Item> q = new DelayQueue<>();
        q.add(new Item(-1_000_000_000L)); // already expired (1s ago)
        check(q.poll() != null, "poll returns an expired item");
        check(q.isEmpty(), "queue empty after poll");
        q.add(new Item(60_000_000_000L)); // 60s in the future
        check(q.poll() == null, "poll returns null for an unexpired item");
        check(q.size() == 1, "size 1 after unexpired add");
    }

    public static void main(String[] args) {
        testDelayQueue();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
