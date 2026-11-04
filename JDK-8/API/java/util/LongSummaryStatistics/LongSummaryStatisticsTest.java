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

    public static void main(String[] args) {
        testStats();
        testCombine();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
