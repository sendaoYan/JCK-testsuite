/*
 * @test
 * @summary java.util.concurrent.atomic.DoubleAdder conformance: add/sum/reset.
 * @run main DoubleAdderTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.DoubleAdder;

public class DoubleAdderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAdder() {
        DoubleAdder adder = new DoubleAdder();
        check(adder.sum() == 0.0, "initial sum 0");
        adder.add(1.5);
        adder.add(2.5);
        check(adder.sum() == 4.0, "add/sum");
        adder.reset();
        check(adder.sum() == 0.0, "reset");
        adder.add(3.0);
        check(adder.sumThenReset() == 3.0, "sumThenReset returns old");
        check(adder.sum() == 0.0, "sumThenReset resets");
        check(adder.toString() != null, "toString");
    }

    // ---- 契约深化：二进制可精确浮点 / 负数 / Number 视图 / sumThenReset 二次 / 序列化 / 双线程 ----
    static void testExactBinarySums() {
        // 只用可精确表示的二进制浮点数（0.5/0.25/0.125）
        DoubleAdder a = new DoubleAdder();
        a.add(0.5);
        a.add(0.25);
        check(a.sum() == 0.75, "0.5 + 0.25 == 0.75 exactly");
        a.add(0.125);
        check(a.sum() == 0.875, "0.875 exact");
        a.reset();
        // 多次 add 后 sum 全等
        for (int i = 0; i < 10; i++) a.add(0.5);
        check(a.sum() == 5.0, "ten 0.5 adds sum to exactly 5.0");
        // 负数 add
        a.add(-2.5);
        check(a.sum() == 2.5, "negative add exact");
        a.add(-2.5);
        check(a.sum() == 0.0, "negative add back to zero");
        check(1.0 / a.sum() == Double.POSITIVE_INFINITY, "sum is +0.0 not -0.0");
    }

    static void testResetSemantics() {
        DoubleAdder a = new DoubleAdder();
        a.add(1.0);
        a.reset();
        check(a.sum() == 0.0, "reset returns to zero");
        check(a.sumThenReset() == 0.0, "sumThenReset after reset returns 0");
        a.add(0.25);
        check(a.sumThenReset() == 0.25, "sumThenReset returns old sum");
        check(a.sumThenReset() == 0.0, "second sumThenReset returns 0");
    }

    static void testNumberViews() {
        DoubleAdder a = new DoubleAdder();
        a.add(0.75);
        check(a.doubleValue() == 0.75, "doubleValue == sum");
        check(a.longValue() == 0L, "longValue truncates sum toward zero");
        check(a.intValue() == 0, "intValue truncates sum toward zero");
        check(a.floatValue() == 0.75f, "floatValue == (float)sum");
        a.add(-0.75);
        check(a.sum() == 0.0, "sum back to zero");
        a.add(2.75);
        check(a.longValue() == 2L, "longValue truncates 2.75 to 2");
        check(a.intValue() == 2, "intValue truncates 2.75 to 2");
        check(a instanceof Number, "DoubleAdder is a Number");
        check(a.toString().equals("2.75"), "toString equals Double.toString(sum)");
    }

    static void testSerialization() throws Exception {
        DoubleAdder a = new DoubleAdder();
        a.add(0.5);
        a.add(0.25);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(a);
        }
        DoubleAdder copy;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            copy = (DoubleAdder) ois.readObject();
        }
        check(copy.sum() == 0.75, "serialization round-trip preserves sum");
        copy.add(0.25);
        check(copy.sum() == 1.0, "deserialized instance remains operational");
    }

    static void testTwoThreadBounded() throws Exception {
        // 两 daemon 线程各 add 1000 次 0.5，join 有界后 sum == 1000.0（精确）
        final DoubleAdder a = new DoubleAdder();
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        Thread t1 = new Thread(() -> {
            try { start.await(); } catch (InterruptedException ignored) { }
            for (int i = 0; i < 1000; i++) a.add(0.5);
        });
        Thread t2 = new Thread(() -> {
            try { start.await(); } catch (InterruptedException ignored) { }
            for (int i = 0; i < 1000; i++) a.add(0.5);
        });
        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();
        start.countDown();
        t1.join(10_000);
        t2.join(10_000);
        check(!t1.isAlive() && !t2.isAlive(), "both worker threads finished within bound");
        check(a.sum() == 1000.0, "two-thread sum is exactly 1000.0");
    }

    public static void main(String[] args) throws Exception {
        testAdder();
        testExactBinarySums();
        testResetSemantics();
        testNumberViews();
        testSerialization();
        testTwoThreadBounded();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
