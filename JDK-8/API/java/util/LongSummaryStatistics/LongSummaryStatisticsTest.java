/*
 * @test
 * @summary java.util.LongSummaryStatistics API conformance: accept, getCount/getSum/getMin/
 *          getMax/getAverage, and combine.
 * @run main LongSummaryStatisticsTest
 */

import java.util.LongSummaryStatistics;

public class LongSummaryStatisticsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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
    }

    static void testCombine() {
        LongSummaryStatistics stats = new LongSummaryStatistics();
        stats.accept(30L);
        stats.accept(10L);
        LongSummaryStatistics other = new LongSummaryStatistics();
        other.accept(20L);
        stats.combine(other);
        check(stats.getCount() == 3 && stats.getSum() == 60L, "combine");
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
            check(true, "4-arg ctor absent on JDK 8");
        } catch (ReflectiveOperationException re) {
            check(false, "4-arg ctor invocation failed");
        }
    }

    public static void main(String[] args) {
        testStats();
        testCombine();
        testSummaryContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
