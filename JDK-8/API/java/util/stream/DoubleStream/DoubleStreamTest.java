/*
 * @test
 * @summary java.util.stream.DoubleStream contract: factories, intermediate/terminal ops,
 *          empty-stream semantics, parallel consistency, null->NPE, bad-arg->IAE,
 *          boxed, single-use.
 * @run main DoubleStreamTest
 */

import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class DoubleStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testFactories() {
        check(DoubleStream.empty().count() == 0, "empty");
        check(DoubleStream.of(1.0, 2.0, 3.0).sum() == 6.0, "of varargs");
        check(DoubleStream.concat(DoubleStream.of(1.0), DoubleStream.of(2.0, 3.0)).sum() == 6.0, "concat");
        check(DoubleStream.generate(() -> 1.0).limit(3).sum() == 3.0, "generate");
        check(DoubleStream.iterate(1.0, x -> x + 1.0).limit(4).sum() == 10.0, "iterate");
        DoubleStream.Builder b = DoubleStream.builder();
        b.add(1.0).add(2.0).add(3.0);
        check(b.build().sum() == 6.0, "builder");
    }

    static void testIntermediate() {
        check(DoubleStream.of(1.0, 2.0, 3.0).map(x -> x * 10.0).sum() == 60.0, "map");
        check(DoubleStream.of(1.0, 2.0, 3.0, 4.0).filter(x -> x % 2 == 0).sum() == 6.0, "filter");
        check(DoubleStream.of(1.0, 2.0).flatMap(x -> DoubleStream.of(x, x)).sum() == 6.0, "flatMap");
        check(DoubleStream.of(3.0, 1.0, 2.0).sorted().toArray()[0] == 1.0, "sorted");
        check(DoubleStream.of(1.0, 1.0, 2.0).distinct().count() == 2, "distinct");
        check(DoubleStream.of(1.0, 2.0, 3.0, 4.0).limit(2).sum() == 3.0, "limit");
        check(DoubleStream.of(1.0, 2.0, 3.0, 4.0).skip(2).sum() == 7.0, "skip");
        List<Double> boxed = DoubleStream.of(1.0, 2.0, 3.0).boxed().collect(Collectors.toList());
        check(boxed.equals(Arrays.asList(1.0, 2.0, 3.0)), "boxed");
        check(DoubleStream.of(1.0, 2.0, 3.0).mapToObj(Double::valueOf).count() == 3, "mapToObj");
        check(DoubleStream.of(1.0, 2.0, 3.0).mapToInt(x -> (int) x).sum() == 6, "mapToInt");
        check(DoubleStream.of(1.0, 2.0, 3.0).mapToLong(x -> (long) x).sum() == 6L, "mapToLong");
    }

    static void testTerminal() {
        check(DoubleStream.of(1.0, 2.0, 3.0, 4.0).sum() == 10.0, "sum");
        check(DoubleStream.empty().sum() == 0.0, "empty sum 0");
        check(DoubleStream.of(5.0, 1.0, 9.0).min().getAsDouble() == 1.0, "min");
        check(DoubleStream.of(5.0, 1.0, 9.0).max().getAsDouble() == 9.0, "max");
        check(!DoubleStream.empty().min().isPresent(), "empty min");
        OptionalDouble avg = DoubleStream.of(1.0, 2.0, 3.0, 4.0).average();
        check(avg.getAsDouble() == 2.5, "average");
        check(!DoubleStream.empty().average().isPresent(), "empty average");
        check(DoubleStream.of(1.0, 2.0, 3.0).count() == 3, "count");
        check(DoubleStream.of(1.0, 2.0, 3.0).reduce(0.0, (a, b) -> a + b) == 6.0, "reduce identity");
        check(DoubleStream.of(1.0, 2.0, 3.0).reduce((a, b) -> a + b).getAsDouble() == 6.0, "reduce no identity");
        java.util.DoubleSummaryStatistics st = DoubleStream.of(1.0, 2.0, 3.0, 4.0).summaryStatistics();
        check(st.getCount() == 4 && st.getSum() == 10.0 && st.getMin() == 1.0 && st.getMax() == 4.0
                && st.getAverage() == 2.5, "summaryStatistics");
        check(DoubleStream.of(1.0, 2.0, 3.0).anyMatch(x -> x == 2.0), "anyMatch");
        check(DoubleStream.of(1.0, 2.0, 3.0).allMatch(x -> x > 0.0), "allMatch");
        check(DoubleStream.of(1.0, 2.0, 3.0).noneMatch(x -> x > 5.0), "noneMatch");
        check(DoubleStream.of(3.0, 2.0, 1.0).findFirst().getAsDouble() == 3.0, "findFirst");
        check(DoubleStream.of(1.0, 2.0, 3.0).findAny().isPresent(), "findAny");
    }

    static void testParallelConsistency() {
        double serial = DoubleStream.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0).sum();
        double parallel = DoubleStream.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0).parallel().sum();
        check(serial == parallel, "parallel sum equals serial");
    }

    static void testSingleUse() {
        DoubleStream s = DoubleStream.of(1.0, 2.0, 3.0);
        check(s.sum() == 6.0, "first consumption");
        checkThrows(IllegalStateException.class, () -> s.sum(), "consumed twice ISE");
    }

    static void testNullAndBadArgs() {
        checkThrows(NullPointerException.class, () -> DoubleStream.of(1.0).filter(null), "filter(null)");
        checkThrows(NullPointerException.class, () -> DoubleStream.of(1.0).map(null), "map(null)");
        checkThrows(IllegalArgumentException.class, () -> DoubleStream.of(1.0).limit(-1), "limit(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> DoubleStream.of(1.0).skip(-1), "skip(-1) IAE");
        checkThrows(NullPointerException.class, () -> DoubleStream.of(1.0).reduce((java.util.function.DoubleBinaryOperator) null), "reduce null accumulator NPE");
        checkThrows(NullPointerException.class, () -> DoubleStream.of(1.0).mapToObj(null), "mapToObj(null)");
    }

    public static void main(String[] args) {
        testFactories();
        testIntermediate();
        testTerminal();
        testParallelConsistency();
        testSingleUse();
        testNullAndBadArgs();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
