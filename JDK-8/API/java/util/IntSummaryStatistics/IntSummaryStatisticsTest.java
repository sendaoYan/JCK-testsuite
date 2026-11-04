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


    // ===================== 契约深化：空态边界/平均值精确/4 参构造(版本差异) =====================
    static void testSummaryContracts() {
        IntSummaryStatistics empty = new IntSummaryStatistics();
        check(empty.getCount() == 0 && empty.getSum() == 0, "empty count/sum zero");
        check(empty.getMin() == Integer.MAX_VALUE && empty.getMax() == Integer.MIN_VALUE,
              "empty min=MAX_VALUE max=MIN_VALUE (identity values)");
        check(empty.getAverage() == 0.0, "empty average 0.0");
        IntSummaryStatistics s = new IntSummaryStatistics();
        s.accept(3); s.accept(-2);
        check(s.getCount() == 2 && s.getSum() == 1, "sum with negative");
        check(s.getMin() == -2 && s.getMax() == 3, "min/max with negative");
        check(s.getAverage() == 0.5, "fractional average of ints");
        check(s.toString() != null && s.toString().length() > 0, "toString non-empty");
        try {
            java.lang.reflect.Constructor<IntSummaryStatistics> c4 =
                  IntSummaryStatistics.class.getConstructor(
                      long.class, int.class, int.class, long.class);
            IntSummaryStatistics built = c4.newInstance(2, -2, 3, 1);
            check(built.getCount() == 2 && built.getSum() == 1, "4-arg ctor (JDK 22+)");
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
