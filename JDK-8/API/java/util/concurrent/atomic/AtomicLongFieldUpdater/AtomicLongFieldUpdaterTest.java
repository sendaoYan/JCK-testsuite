/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicLongFieldUpdater conformance: field updates.
 * @run main AtomicLongFieldUpdaterTest
 */

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public class AtomicLongFieldUpdaterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Counter {
        public volatile long value;
    }

    static void testUpdater() {
        AtomicLongFieldUpdater<Counter> u = AtomicLongFieldUpdater.newUpdater(Counter.class, "value");
        Counter c = new Counter();
        check(u.get(c) == 0L, "initial value 0");
        u.set(c, 5L);
        check(u.get(c) == 5L, "set/get");
        check(u.compareAndSet(c, 5L, 7L), "compareAndSet true");
        check(u.get(c) == 7L, "compareAndSet applied");
        check(!u.compareAndSet(c, 5L, 9L), "compareAndSet false");
        check(u.weakCompareAndSet(c, 7L, 8L), "weakCompareAndSet");
        check(u.getAndIncrement(c) == 8L, "getAndIncrement returns old");
        check(u.get(c) == 9L, "incremented");
        check(u.incrementAndGet(c) == 10L, "incrementAndGet");
        check(u.getAndDecrement(c) == 10L, "getAndDecrement returns old");
        check(u.decrementAndGet(c) == 8L, "decrementAndGet");
        check(u.getAndAdd(c, 10L) == 8L, "getAndAdd returns old");
        check(u.addAndGet(c, 1L) == 19L, "addAndGet");
        check(u.getAndSet(c, 100L) == 19L, "getAndSet returns old");
        check(u.get(c) == 100L, "getAndSet applied");
        check(u.getAndUpdate(c, x -> x + 1L) == 100L, "getAndUpdate");
        check(u.get(c) == 101L, "after getAndUpdate");
        check(u.updateAndGet(c, x -> x + 1L) == 102L, "updateAndGet");
        check(u.getAndAccumulate(c, 5L, (a, b) -> a + b) == 102L, "getAndAccumulate");
        check(u.accumulateAndGet(c, 5L, (a, b) -> a + b) == 112L, "accumulateAndGet");
        u.lazySet(c, 42L);
        check(u.get(c) == 42L, "lazySet");
    }

    public static void main(String[] args) {
        testUpdater();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
