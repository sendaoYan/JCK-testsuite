/*
 * @test
 * @summary java.util.IntSummaryStatistics conformance: white-box structure plus black-box behavior
 *          of accept/getCount/getSum/getMin/getMax/getAverage, combine, the (count,min,max,sum)
 *          constructor, and empty statistics.
 * @run main IntSummaryStatisticsTest
 */

import java.util.IntSummaryStatistics;
import java.util.function.IntConsumer;

public class IntSummaryStatisticsTest {
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
        Class<?> c = Class.forName("java.util.IntSummaryStatistics");
        check(!c.isInterface(), "IntSummaryStatistics is a class");
        check(IntConsumer.class.isAssignableFrom(c), "implements IntConsumer");
        c.getMethod("accept", int.class);
        c.getMethod("combine", IntSummaryStatistics.class);
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
        IntSummaryStatistics stats = new IntSummaryStatistics();
        stats.accept(3);
        stats.accept(1);
        stats.accept(2);
        check(stats.getCount() == 3, "count");
        check(stats.getSum() == 6, "sum");
        check(stats.getMin() == 1 && stats.getMax() == 3, "min/max");
        check(stats.getAverage() == 2.0, "average");
        check(stats.toString() != null, "toString non-null");
    }

    static void testCombine() {
        IntSummaryStatistics stats = new IntSummaryStatistics();
        stats.accept(3);
        stats.accept(1);
        stats.accept(2);
        IntSummaryStatistics other = new IntSummaryStatistics();
        other.accept(4);
        stats.combine(other);
        check(stats.getCount() == 4 && stats.getSum() == 10, "combine count/sum");
        check(stats.getMin() == 1 && stats.getMax() == 4, "combine min/max");
    }

    static void testConstructor() {
        IntSummaryStatistics stats = new IntSummaryStatistics(5, 1, 9, 30);
        check(stats.getCount() == 5, "constructor count");
        check(stats.getSum() == 30, "constructor sum");
        check(stats.getMin() == 1 && stats.getMax() == 9, "constructor min/max");
        check(stats.getAverage() == 6.0, "constructor average");
        assertThrows(IllegalArgumentException.class,
                () -> new IntSummaryStatistics(-1, 1, 9, 30),
                "constructor negative count throws IAE");
    }

    static void testEmpty() {
        IntSummaryStatistics stats = new IntSummaryStatistics();
        check(stats.getCount() == 0, "empty count 0");
        check(stats.getSum() == 0, "empty sum 0");
        check(stats.getMin() == Integer.MAX_VALUE, "empty min == Integer.MAX_VALUE");
        check(stats.getMax() == Integer.MIN_VALUE, "empty max == Integer.MIN_VALUE");
        check(stats.getAverage() == 0.0, "empty average 0.0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
