/*
 * @test
 * @summary java.util.DoubleSummaryStatistics conformance: white-box structure plus black-box
 *          behavior of accept/getCount/getSum/getMin/getMax/getAverage, combine, the
 *          (count,min,max,sum) constructor, and empty statistics.
 * @run main DoubleSummaryStatisticsTest
 */

import java.util.DoubleSummaryStatistics;
import java.util.function.DoubleConsumer;

public class DoubleSummaryStatisticsTest {
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
        Class<?> c = Class.forName("java.util.DoubleSummaryStatistics");
        check(!c.isInterface(), "DoubleSummaryStatistics is a class");
        check(DoubleConsumer.class.isAssignableFrom(c), "implements DoubleConsumer");
        c.getMethod("accept", double.class);
        c.getMethod("combine", DoubleSummaryStatistics.class);
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
        check(stats.getCount() == 3 && stats.getSum() == 6.0, "combine count/sum");
        check(stats.getMin() == 1.0 && stats.getMax() == 3.0, "combine min/max");
    }

    static void testConstructor() {
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics(5, 1.0, 9.0, 30.0);
        check(stats.getCount() == 5, "constructor count");
        check(stats.getSum() == 30.0, "constructor sum");
        check(stats.getMin() == 1.0 && stats.getMax() == 9.0, "constructor min/max");
        check(stats.getAverage() == 6.0, "constructor average");
        assertThrows(IllegalArgumentException.class,
                () -> new DoubleSummaryStatistics(-1, 1.0, 9.0, 30.0),
                "constructor negative count throws IAE");
    }

    static void testEmpty() {
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        check(stats.getCount() == 0, "empty count 0");
        check(stats.getSum() == 0.0, "empty sum 0");
        check(stats.getMin() == Double.POSITIVE_INFINITY, "empty min == +Infinity");
        check(stats.getMax() == Double.NEGATIVE_INFINITY, "empty max == -Infinity");
        check(stats.getAverage() == 0.0, "empty average 0.0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
