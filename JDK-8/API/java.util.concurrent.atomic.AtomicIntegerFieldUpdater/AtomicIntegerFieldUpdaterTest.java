/*
 * @test
 * @summary java.util.concurrent.atomic.AtomicIntegerFieldUpdater conformance: field updates.
 * @run main AtomicIntegerFieldUpdaterTest
 */

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class AtomicIntegerFieldUpdaterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Counter {
        public volatile int value;
    }

    static void testUpdater() {
        AtomicIntegerFieldUpdater<Counter> u = AtomicIntegerFieldUpdater.newUpdater(Counter.class, "value");
        Counter c = new Counter();
        check(u.get(c) == 0, "initial value 0");
        u.set(c, 5);
        check(u.get(c) == 5, "set/get");
        check(u.compareAndSet(c, 5, 7), "compareAndSet true");
        check(u.get(c) == 7, "compareAndSet applied");
        check(!u.compareAndSet(c, 5, 9), "compareAndSet false");
        check(u.weakCompareAndSet(c, 7, 8), "weakCompareAndSet");
        check(u.getAndIncrement(c) == 8, "getAndIncrement returns old");
        check(u.get(c) == 9, "incremented");
        check(u.incrementAndGet(c) == 10, "incrementAndGet");
        check(u.getAndDecrement(c) == 10, "getAndDecrement returns old");
        check(u.decrementAndGet(c) == 8, "decrementAndGet");
        check(u.getAndAdd(c, 10) == 8, "getAndAdd returns old");
        check(u.addAndGet(c, 1) == 19, "addAndGet");
        check(u.getAndSet(c, 100) == 19, "getAndSet returns old");
        check(u.get(c) == 100, "getAndSet applied");
        check(u.getAndUpdate(c, x -> x + 1) == 100, "getAndUpdate");
        check(u.get(c) == 101, "after getAndUpdate");
        check(u.updateAndGet(c, x -> x + 1) == 102, "updateAndGet");
        check(u.getAndAccumulate(c, 5, (a, b) -> a + b) == 102, "getAndAccumulate");
        check(u.accumulateAndGet(c, 5, (a, b) -> a + b) == 112, "accumulateAndGet");
        u.lazySet(c, 42);
        check(u.get(c) == 42, "lazySet");
    }

    public static void main(String[] args) {
        testUpdater();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
