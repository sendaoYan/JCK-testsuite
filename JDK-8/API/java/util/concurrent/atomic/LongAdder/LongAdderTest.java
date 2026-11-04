/*
 * @test
 * @summary java.util.concurrent.atomic.LongAdder conformance: white-box structure plus
 *          black-box behavior of add/increment/decrement/sum/reset/sumThenReset, Number views,
 *          toString, serialization round-trip, and a concurrent add with an exact final sum.
 * @run main LongAdderTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.LongAdder;

public class LongAdderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void await(java.util.concurrent.CountDownLatch latch) {
        try { latch.await(); } catch (InterruptedException e) { throw new RuntimeException(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.atomic.LongAdder");
        check(!c.isInterface(), "LongAdder is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "LongAdder is concrete");
        check(c.getSuperclass() == Class.forName("java.util.concurrent.atomic.Striped64"), "LongAdder extends Striped64");
        check(Serializable.class.isAssignableFrom(c), "LongAdder implements Serializable");
        c.getConstructor();
        c.getMethod("add", long.class);
        c.getMethod("increment");
        c.getMethod("decrement");
        c.getMethod("sum");
        c.getMethod("sumThenReset");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testSingleThread();
        testNumberViews();
        testSerialization();
        testConcurrent();
    }

    static void testSingleThread() {
        LongAdder a = new LongAdder();
        check(a.sum() == 0L, "initial sum 0");

        a.add(5L);
        a.add(7L);
        check(a.sum() == 12L, "add/sum");

        a.increment();
        check(a.sum() == 13L, "increment");
        a.decrement();
        check(a.sum() == 12L, "decrement");

        a.reset();
        check(a.sum() == 0L, "reset returns sum to 0");

        a.add(42L);
        check(a.sumThenReset() == 42L, "sumThenReset returns old sum");
        check(a.sum() == 0L, "sumThenReset resets to 0");

        a.add(9L);
        check(a.toString().equals("9"), "toString is Long.toString(sum)");
    }

    static void testNumberViews() {
        LongAdder a = new LongAdder();
        a.add(123L);
        check(a.longValue() == 123L, "longValue == sum");
        check(a.intValue() == 123, "intValue == (int)sum");
        check(a.floatValue() == 123.0f, "floatValue == (float)sum");
        check(a.doubleValue() == 123.0, "doubleValue == (double)sum");
    }

    @SuppressWarnings("unchecked")
    static void testSerialization() throws Exception {
        LongAdder a = new LongAdder();
        a.add(7L);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(a);
        }
        LongAdder copy;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            copy = (LongAdder) ois.readObject();
        }
        check(copy.sum() == 7L, "serialization round-trip preserves sum");
    }

    static void testConcurrent() throws Exception {
        final int N = 8, M = 100_000;
        final LongAdder a = new LongAdder();
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread[] ts = new Thread[N];
        for (int i = 0; i < N; i++) {
            ts[i] = new Thread(() -> {
                await(start);
                for (int j = 0; j < M; j++) a.add(1L);
            });
            ts[i].start();
        }
        start.countDown();
        for (Thread t : ts) t.join();
        check(a.sum() == (long) N * M, "concurrent add loses no updates (exact long sum)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
