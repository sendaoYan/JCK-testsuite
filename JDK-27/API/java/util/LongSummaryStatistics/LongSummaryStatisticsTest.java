/*
 * @test
 * @summary java.util.LongSummaryStatistics conformance: white-box structure plus black-box behavior
 *          of accept(long)/accept(int)/getCount/getSum/getMin/getMax/getAverage, combine, the
 *          (count,min,max,sum) constructor, and empty statistics.
 * @run main LongSummaryStatisticsTest
 */

import java.util.LongSummaryStatistics;
import java.util.function.LongConsumer;

public class LongSummaryStatisticsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.LongSummaryStatistics");
        check(!c.isInterface(), "LongSummaryStatistics is a class");
        check(LongConsumer.class.isAssignableFrom(c), "implements LongConsumer");
        c.getMethod("accept", long.class);
        c.getMethod("accept", int.class);
        c.getMethod("combine", LongSummaryStatistics.class);
        c.getMethod("getCount");
        c.getMethod("getSum");
        c.getMethod("getMin");
        c.getMethod("getMax");
        c.getMethod("getAverage");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testStats();
        testCombine();
        testConstructor();
        testEmpty();
    }

    static void testStats() {
        LongSummaryStatistics stats = new LongSummaryStatistics();
        stats.accept(30L);
        stats.accept(10L);
        stats.accept(20L);
        check(stats.getCount() == 3, "count");
        check(stats.getSum() == 60L, "sum");
        check(stats.getMin() == 10L && stats.getMax() == 30L, "min/max");
        check(stats.getAverage() == 20.0, "average");
        stats.accept(7); // accept(int) widens to long
        check(stats.getCount() == 4 && stats.getSum() == 67L, "accept(int) widens");
    }

    static void testCombine() {
        LongSummaryStatistics stats = new LongSummaryStatistics();
        stats.accept(30L);
        stats.accept(10L);
        LongSummaryStatistics other = new LongSummaryStatistics();
        other.accept(20L);
        stats.combine(other);
        check(stats.getCount() == 3 && stats.getSum() == 60L, "combine count/sum");
        check(stats.getMin() == 10L && stats.getMax() == 30L, "combine min/max");
    }

    static void testConstructor() {
        LongSummaryStatistics stats = new LongSummaryStatistics(5, 1L, 9L, 30L);
        check(stats.getCount() == 5, "constructor count");
        check(stats.getSum() == 30L, "constructor sum");
        check(stats.getMin() == 1L && stats.getMax() == 9L, "constructor min/max");
        check(stats.getAverage() == 6.0, "constructor average");
        assertThrows(IllegalArgumentException.class,
                () -> new LongSummaryStatistics(-1, 1L, 9L, 30L),
                "constructor negative count throws IAE");
    }

    static void testEmpty() {
        LongSummaryStatistics stats = new LongSummaryStatistics();
        check(stats.getCount() == 0, "empty count 0");
        check(stats.getSum() == 0L, "empty sum 0");
        check(stats.getMin() == Long.MAX_VALUE, "empty min == Long.MAX_VALUE");
        check(stats.getMax() == Long.MIN_VALUE, "empty max == Long.MIN_VALUE");
        check(stats.getAverage() == 0.0, "empty average 0.0");
    }


    // ===================== 契约深化：空态边界/负值序列/4 参构造(版本差异) =====================
    static void testSummaryContracts() {
        LongSummaryStatistics empty = new LongSummaryStatistics();
        check(empty.getCount() == 0 && empty.getSum() == 0L, "empty count/sum zero");
        check(empty.getMin() == Long.MAX_VALUE && empty.getMax() == Long.MIN_VALUE,
              "empty min/max identity values");
        check(empty.getAverage() == 0.0, "empty average 0.0");
        LongSummaryStatistics s = new LongSummaryStatistics();
        s.accept(100L); s.accept(-50L);
        check(s.getCount() == 2 && s.getSum() == 50L, "sum with negative");
        check(s.getMin() == -50L && s.getMax() == 100L, "min/max");
        check(s.getAverage() == 25.0, "average");
        check(s.toString() != null && s.toString().length() > 0, "toString non-empty");
        try {
            java.lang.reflect.Constructor<LongSummaryStatistics> c4 =
                  LongSummaryStatistics.class.getConstructor(
                      long.class, long.class, long.class, long.class);
            LongSummaryStatistics built = c4.newInstance(2, -50L, 100L, 50L);
            check(built.getCount() == 2 && built.getSum() == 50L, "4-arg ctor (JDK 22+)");
        } catch (NoSuchMethodException nsme) {
            check(false, "4-arg ctor expected present");
        } catch (ReflectiveOperationException re) {
            check(false, "4-arg ctor invocation failed");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSummaryContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
