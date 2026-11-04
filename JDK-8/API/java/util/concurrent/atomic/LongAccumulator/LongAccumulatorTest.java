/*
 * @test
 * @summary java.util.concurrent.atomic.LongAccumulator conformance: accumulate/get/reset.
 * @run main LongAccumulatorTest
 */

import java.util.concurrent.atomic.LongAccumulator;

public class LongAccumulatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAccumulator() {
        LongAccumulator acc = new LongAccumulator((a, b) -> a * b, 1L);
        check(acc.get() == 1L, "identity");
        acc.accumulate(2L);
        acc.accumulate(3L);
        check(acc.get() == 6L, "accumulate/get");
        acc.reset();
        check(acc.get() == 1L, "reset to identity");
        acc.accumulate(4L);
        check(acc.getThenReset() == 4L, "getThenReset returns old");
        check(acc.get() == 1L, "getThenReset resets");
        check(acc.toString() != null, "toString");
    }

    // ---- 契约深化：Long::sum / 64 位环绕契约 / max 函数 / Number 视图 ----
    static void testSumAccumulator() {
        LongAccumulator acc = new LongAccumulator(Long::sum, 0L);
        check(acc.get() == 0L, "sum identity is 0");
        acc.accumulate(10L);
        acc.accumulate(20L);
        acc.accumulate(30L);
        check(acc.get() == 60L, "Long::sum accumulate exact");
        check(acc.toString().equals("60"), "toString == Long.toString(get)");
        acc.reset();
        check(acc.get() == 0L, "reset returns sum to identity 0");
        // getThenReset 链
        acc.accumulate(-5L);
        check(acc.getThenReset() == -5L, "getThenReset returns value");
        check(acc.get() == 0L, "getThenReset resets to identity");
        check(acc.getThenReset() == 0L, "second getThenReset returns identity");
    }

    static void testOverflowWrapContract() {
        // 经典契约点（JDK-8 实测）：sum()/get() 按补码 64 位环绕，不抛 ArithmeticException
        LongAccumulator acc = new LongAccumulator(Long::sum, 0L);
        acc.accumulate(Long.MAX_VALUE);
        acc.accumulate(Long.MAX_VALUE);
        check(acc.get() == -2L, "MAX_VALUE twice wraps to -2 (no ArithmeticException)");
        check(acc.longValue() == -2L, "longValue matches wrapped sum");
        check(acc.intValue() == -2, "intValue of wrapped sum");
        LongAccumulator neg = new LongAccumulator(Long::sum, 0L);
        neg.accumulate(Long.MIN_VALUE);
        neg.accumulate(-1L);
        check(neg.get() == Long.MAX_VALUE, "MIN_VALUE + (-1) wraps to MAX_VALUE");
        check(neg.doubleValue() == 9.223372036854776E18, "doubleValue of wrapped MAX");
    }

    static void testMaxAccumulator() {
        // 自定义 max 函数：identity 为极小值
        LongAccumulator max = new LongAccumulator(Math::max, Long.MIN_VALUE);
        check(max.get() == Long.MIN_VALUE, "max identity");
        max.accumulate(5L);
        max.accumulate(3L);
        max.accumulate(9L);
        max.accumulate(-1L);
        check(max.get() == 9L, "Math::max keeps maximum");
        max.accumulate(9L);
        check(max.get() == 9L, "accumulating equal max keeps value");
        max.reset();
        check(max.get() == Long.MIN_VALUE, "reset returns to max identity");
        max.accumulate(-100L);
        check(max.getThenReset() == -100L, "getThenReset on max accumulator");
        check(max.get() == Long.MIN_VALUE, "max accumulator reset to identity");
    }

    static void testNumberViews() {
        LongAccumulator acc = new LongAccumulator(Long::sum, 0L);
        acc.accumulate(60L);
        check(acc.longValue() == 60L, "longValue == get");
        check(acc.intValue() == 60, "intValue == (int)get");
        check(acc.floatValue() == 60.0f, "floatValue == (float)get");
        check(acc.doubleValue() == 60.0, "doubleValue == (double)get");
        check(acc instanceof Number, "LongAccumulator is a Number");
    }

    public static void main(String[] args) {
        testAccumulator();
        testSumAccumulator();
        testOverflowWrapContract();
        testMaxAccumulator();
        testNumberViews();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
