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


    // ===================== 契约深化：空态边界/负值序列/toString/4 参构造(版本差异) =====================
    static void testSummaryContracts() {
        DoubleSummaryStatistics empty = new DoubleSummaryStatistics();
        check(empty.getCount() == 0 && empty.getSum() == 0.0, "empty count/sum zero");
        check(empty.getMin() == Double.POSITIVE_INFINITY
              && empty.getMax() == Double.NEGATIVE_INFINITY,
              "empty min=+Inf max=-Inf (identity values)");
        check(empty.getAverage() == 0.0, "empty average 0.0");
        DoubleSummaryStatistics s = new DoubleSummaryStatistics();
        s.accept(2.5); s.accept(7.5); s.accept(-1.0);
        check(s.getCount() == 3 && s.getSum() == 9.0, "sequence sum");
        check(s.getMin() == -1.0 && s.getMax() == 7.5, "negative min tracked");
        check(s.getAverage() == 3.0, "average");
        check(s.toString() != null && s.toString().length() > 0, "toString non-empty");
        DoubleSummaryStatistics neg = new DoubleSummaryStatistics();
        neg.accept(-5.0);
        check(neg.getAverage() == -5.0 && neg.getMin() == -5.0 && neg.getMax() == -5.0,
              "single negative element");
        // 4 参构造为 JDK 22+ 新增：JDK-8 不存在
        try {
            java.lang.reflect.Constructor<DoubleSummaryStatistics> c4 =
                  DoubleSummaryStatistics.class.getConstructor(
                      long.class, double.class, double.class, double.class);
            DoubleSummaryStatistics built = c4.newInstance(3, -1.0, 7.5, 9.0);
            check(built.getCount() == 3 && built.getSum() == 9.0 && built.getAverage() == 3.0,
                  "4-arg ctor precomputed statistics (JDK 22+)");
            try {
                c4.newInstance(2, 5.0, -1.0, 1.0);
                check(false, "4-arg ctor max<min IAE");
            } catch (java.lang.reflect.InvocationTargetException ite) {
                check(ite.getCause() instanceof IllegalArgumentException,
                      "4-arg ctor max<min IAE");
            }
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
