/*
 * @test
 * @summary java.util.concurrent.atomic.DoubleAccumulator conformance: accumulate/get/reset.
 * @run main DoubleAccumulatorTest
 */

import java.util.concurrent.atomic.DoubleAccumulator;

public class DoubleAccumulatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAccumulator() {
        DoubleAccumulator acc = new DoubleAccumulator((a, b) -> a + b, 0.0);
        check(acc.get() == 0.0, "identity");
        acc.accumulate(2.0);
        acc.accumulate(3.0);
        check(acc.get() == 5.0, "accumulate/get");
        acc.reset();
        check(acc.get() == 0.0, "reset to identity");
        acc.accumulate(4.0);
        check(acc.getThenReset() == 4.0, "getThenReset returns old");
        check(acc.get() == 0.0, "getThenReset resets");
        check(acc.toString() != null, "toString");
    }

    // ---- 契约深化：二进制可精确浮点 / max 函数 / Number 视图 / getThenReset 链 ----
    static void testExactBinarySums() {
        DoubleAccumulator acc = new DoubleAccumulator(Double::sum, 0.0);
        check(acc.get() == 0.0, "Double::sum identity 0.0");
        acc.accumulate(0.5);
        acc.accumulate(0.25);
        check(acc.get() == 0.75, "0.5 + 0.25 == 0.75 exactly");
        acc.accumulate(0.125);
        check(acc.get() == 0.875, "0.875 exact");
        acc.reset();
        check(acc.get() == 0.0, "reset to identity");
        // 负数
        acc.accumulate(-0.5);
        acc.accumulate(0.25);
        check(acc.get() == -0.25, "-0.5 + 0.25 == -0.25 exactly");
        // getThenReset 链：第二次返回 identity
        check(acc.getThenReset() == -0.25, "getThenReset returns current value");
        check(acc.getThenReset() == 0.0, "second getThenReset returns identity");
    }

    static void testMaxAccumulator() {
        DoubleAccumulator max = new DoubleAccumulator(Math::max, Double.NEGATIVE_INFINITY);
        check(max.get() == Double.NEGATIVE_INFINITY, "max identity -Inf");
        max.accumulate(0.5);
        max.accumulate(0.125);
        max.accumulate(0.75);
        check(max.get() == 0.75, "Math::max keeps maximum (exact binary fractions)");
        max.accumulate(-1.0);
        check(max.get() == 0.75, "smaller value does not change max");
        max.reset();
        check(max.get() == Double.NEGATIVE_INFINITY, "reset returns to max identity");
    }

    static void testNumberViews() {
        DoubleAccumulator acc = new DoubleAccumulator(Double::sum, 0.0);
        acc.accumulate(2.75);
        check(acc.doubleValue() == 2.75, "doubleValue == get");
        check(acc.floatValue() == 2.75f, "floatValue == (float)get");
        check(acc.longValue() == 2L, "longValue truncates 2.75 to 2");
        check(acc.intValue() == 2, "intValue truncates 2.75 to 2");
        check(acc instanceof Number, "DoubleAccumulator is a Number");
        check(acc.toString().equals("2.75"), "toString == Double.toString(get)");
    }

    public static void main(String[] args) {
        testAccumulator();
        testExactBinarySums();
        testMaxAccumulator();
        testNumberViews();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
