/*
 * @test
 * @summary java.util.DoubleSummaryStatistics API conformance: accept, getCount/getSum/getMin/
 *          getMax/getAverage, and combine.
 * @run main DoubleSummaryStatisticsTest
 */

import java.util.DoubleSummaryStatistics;

public class DoubleSummaryStatisticsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStats() {
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        stats.accept(3.0);
        stats.accept(1.0);
        stats.accept(2.0);
        check(stats.getCount() == 3, "count");
        check(stats.getSum() == 6.0, "sum");
        check(stats.getMin() == 1.0 && stats.getMax() == 3.0, "min/max");
        check(stats.getAverage() == 2.0, "average");
    }

    static void testCombine() {
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        stats.accept(3.0);
        stats.accept(1.0);
        DoubleSummaryStatistics other = new DoubleSummaryStatistics();
        other.accept(2.0);
        stats.combine(other);
        check(stats.getCount() == 3 && stats.getSum() == 6.0, "combine");
    }

    public static void main(String[] args) {
        testStats();
        testCombine();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
