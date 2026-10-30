/*
 * @test
 * @summary java.util.IntSummaryStatistics API conformance: accept, getCount/getSum/getMin/
 *          getMax/getAverage, and combine.
 * @run main IntSummaryStatisticsTest
 */

import java.util.IntSummaryStatistics;

public class IntSummaryStatisticsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStats() {
        IntSummaryStatistics stats = new IntSummaryStatistics();
        stats.accept(3);
        stats.accept(1);
        stats.accept(2);
        check(stats.getCount() == 3, "count");
        check(stats.getSum() == 6, "sum");
        check(stats.getMin() == 1 && stats.getMax() == 3, "min/max");
        check(stats.getAverage() == 2.0, "average");
    }

    static void testCombine() {
        IntSummaryStatistics stats = new IntSummaryStatistics();
        stats.accept(3);
        stats.accept(1);
        stats.accept(2);
        IntSummaryStatistics other = new IntSummaryStatistics();
        other.accept(4);
        stats.combine(other);
        check(stats.getCount() == 4 && stats.getSum() == 10, "combine");
    }

    public static void main(String[] args) {
        testStats();
        testCombine();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
