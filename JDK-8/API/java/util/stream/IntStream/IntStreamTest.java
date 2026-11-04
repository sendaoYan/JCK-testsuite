/*
 * @test
 * @summary java.util.stream.IntStream contract: factories (range/rangeClosed/iterate/
 *          generate), intermediate ops, terminal ops (sum/min/max/average/count/
 *          summaryStatistics/reduce/collect), empty-stream semantics, parallel
 *          consistency, null->NPE, bad-arg->IAE, boxed/asLong/asDouble, single-use.
 * @run main IntStreamTest
 */

import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IntStreamTest {
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
        check(IntStream.empty().count() == 0, "empty");
        check(IntStream.of(1, 2, 3).sum() == 6, "of varargs");
        check(IntStream.concat(IntStream.of(1), IntStream.of(2, 3)).sum() == 6, "concat");
        check(IntStream.generate(() -> 1).limit(3).sum() == 3, "generate");
        check(IntStream.iterate(1, x -> x + 1).limit(4).sum() == 10, "iterate");
        int[] ranged = IntStream.range(1, 5).toArray();
        check(Arrays.equals(ranged, new int[]{1, 2, 3, 4}), "range exclusive");
        int[] closed = IntStream.rangeClosed(1, 4).toArray();
        check(Arrays.equals(closed, new int[]{1, 2, 3, 4}), "rangeClosed inclusive");
        check(IntStream.range(3, 3).count() == 0, "range empty when start==end");
        IntStream.Builder b = IntStream.builder();
        b.add(1).add(2).add(3);
        check(b.build().sum() == 6, "builder");
    }

    static void testIntermediate() {
        check(IntStream.of(1, 2, 3).map(x -> x * 10).sum() == 60, "map");
        check(IntStream.of(1, 2, 3, 4).filter(x -> x % 2 == 0).sum() == 6, "filter");
        check(IntStream.of(1, 2).flatMap(x -> IntStream.of(x, x)).sum() == 6, "flatMap");
        check(IntStream.of(3, 1, 2).sorted().toArray()[0] == 1, "sorted");
        check(IntStream.of(1, 1, 2).distinct().count() == 2, "distinct");
        check(IntStream.of(1, 2, 3, 4).limit(2).sum() == 3, "limit");
        check(IntStream.of(1, 2, 3, 4).skip(2).sum() == 7, "skip");
        List<Integer> boxed = IntStream.of(1, 2, 3).boxed().collect(Collectors.toList());
        check(boxed.equals(Arrays.asList(1, 2, 3)), "boxed");
        check(IntStream.of(1, 2).asLongStream().sum() == 3L, "asLongStream");
        check(IntStream.of(1, 2).asDoubleStream().sum() == 3.0, "asDoubleStream");
        check(IntStream.of(1, 2, 3).mapToObj(Integer::valueOf).count() == 3, "mapToObj");
        check(IntStream.of(1, 2, 3).mapToLong(x -> x * 2L).sum() == 12L, "mapToLong");
        check(IntStream.of(1, 2, 3).mapToDouble(x -> x * 2.0).sum() == 12.0, "mapToDouble");
    }

    static void testTerminal() {
        check(IntStream.of(1, 2, 3, 4).sum() == 10, "sum");
        check(IntStream.empty().sum() == 0, "empty sum 0");
        check(IntStream.of(5, 1, 9).min().getAsInt() == 1, "min");
        check(IntStream.of(5, 1, 9).max().getAsInt() == 9, "max");
        check(!IntStream.empty().min().isPresent(), "empty min");
        check(!IntStream.empty().max().isPresent(), "empty max");
        OptionalDouble avg = IntStream.of(1, 2, 3, 4).average();
        check(avg.getAsDouble() == 2.5, "average");
        check(!IntStream.empty().average().isPresent(), "empty average");
        check(IntStream.of(1, 2, 3).count() == 3, "count");
        check(IntStream.of(1, 2, 3).reduce(0, (a, b) -> a + b) == 6, "reduce identity");
        check(IntStream.of(1, 2, 3).reduce((a, b) -> a + b).getAsInt() == 6, "reduce no identity");
        check(!IntStream.empty().reduce((a, b) -> a + b).isPresent(), "empty reduce");
        java.util.IntSummaryStatistics st = IntStream.of(1, 2, 3, 4).summaryStatistics();
        check(st.getCount() == 4 && st.getSum() == 10 && st.getMin() == 1 && st.getMax() == 4
                && st.getAverage() == 2.5, "summaryStatistics");
        check(IntStream.of(1, 2, 3).anyMatch(x -> x == 2), "anyMatch");
        check(IntStream.of(1, 2, 3).allMatch(x -> x > 0), "allMatch");
        check(IntStream.of(1, 2, 3).noneMatch(x -> x > 5), "noneMatch");
        check(IntStream.of(3, 2, 1).findFirst().getAsInt() == 3, "findFirst");
        check(IntStream.of(1, 2, 3).findAny().isPresent(), "findAny");
    }

    static void testToArrayAndReduce() {
        int[] a = IntStream.of(1, 2, 3).toArray();
        check(a.length == 3 && a[2] == 3, "toArray");
        check(IntStream.of(1, 2, 3).reduce(10, (x, y) -> x + y) == 16, "reduce non-zero identity");
    }

    static void testParallelConsistency() {
        int serial = IntStream.range(1, 100).sum();
        int parallel = IntStream.range(1, 100).parallel().sum();
        check(serial == parallel, "parallel sum equals serial");
        int[] sorted = IntStream.of(3, 1, 2).parallel().sorted().toArray();
        check(Arrays.equals(sorted, new int[]{1, 2, 3}), "parallel sorted");
    }

    static void testSingleUse() {
        IntStream s = IntStream.of(1, 2, 3);
        check(s.sum() == 6, "first consumption");
        checkThrows(IllegalStateException.class, () -> s.sum(), "consumed twice ISE");
    }

    static void testNullAndBadArgs() {
        checkThrows(NullPointerException.class, () -> IntStream.of(1).filter(null), "filter(null)");
        checkThrows(NullPointerException.class, () -> IntStream.of(1).map(null), "map(null)");
        checkThrows(NullPointerException.class, () -> IntStream.of(1).flatMap(null), "flatMap(null)");
        checkThrows(IllegalArgumentException.class, () -> IntStream.of(1).limit(-1), "limit(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> IntStream.of(1).skip(-1), "skip(-1) IAE");
        checkThrows(NullPointerException.class, () -> IntStream.of(1).reduce((java.util.function.IntBinaryOperator) null), "reduce null accumulator NPE");
        checkThrows(NullPointerException.class, () -> IntStream.of(1).mapToObj(null), "mapToObj(null)");
    }


    // ===================== 契约深化：range/rangeClosed 边界/统计矩阵/boxed/asXXXStream/iterate/gauss 并行 =====================
    static void testContracts() {
        // range [1,5) 4 元素 vs rangeClosed [1,5] 5 元素
        check(IntStream.range(1, 5).count() == 4, "range excludes end");
        check(IntStream.rangeClosed(1, 5).count() == 5, "rangeClosed includes end");
        check(IntStream.range(5, 1).count() == 0, "reversed range empty");
        check(IntStream.rangeClosed(5, 5).count() == 1 && IntStream.range(5, 5).count() == 0,
              "single-point rangeClosed only");
        // Gauss：1..100 求和（顺序 = 并行 = 公式）
        int serial = IntStream.rangeClosed(1, 100).sum();
        int parallel = IntStream.rangeClosed(1, 100).parallel().sum();
        check(serial == 5050 && parallel == 5050, "gauss sum serial==parallel==5050");
        // 空流统计契约
        check(IntStream.empty().sum() == 0, "empty sum 0");
        check(!IntStream.empty().average().isPresent(), "empty average empty");
        check(!IntStream.empty().min().isPresent() && !IntStream.empty().max().isPresent(), "empty min/max");
        check(IntStream.empty().summaryStatistics().getCount() == 0, "empty stats count 0");
        // summaryStatistics 全字段
        java.util.IntSummaryStatistics st = IntStream.of(3, 1, 4, 1, 5).summaryStatistics();
        check(st.getCount() == 5 && st.getSum() == 14 && st.getMin() == 1 && st.getMax() == 5
              && st.getAverage() == 2.8, "IntSummaryStatistics all fields");
        // boxed / mapToObj / asLongStream / asDoubleStream
        check(IntStream.of(1, 2).boxed().collect(Collectors.toList()).equals(Arrays.asList(1, 2)), "boxed");
        check(IntStream.of(1, 2).mapToObj(i -> "<" + i + ">").collect(Collectors.toList())
              .equals(Arrays.asList("<1>", "<2>")), "mapToObj");
        check(IntStream.of(1, 2).asLongStream().sum() == 3L, "asLongStream");
        check(IntStream.of(1, 2).asDoubleStream().average().getAsDouble() == 1.5, "asDoubleStream");
        // iterate 双参 + limit
        check(IntStream.iterate(2, i -> i * i).limit(3).toArray().length == 3, "iterate squares bounded");
        // of/concat/distinct/sorted
        check(IntStream.concat(IntStream.of(1, 2), IntStream.of(2, 3)).distinct().toArray().length == 3,
              "concat + distinct");
        check(java.util.Arrays.equals(IntStream.of(3, 1, 2).sorted().toArray(), new int[] {1, 2, 3}),
              "sorted primitives");
        // limit/skip 负例（IAE）
        checkThrows(IllegalArgumentException.class, () -> IntStream.of(1).limit(-1), "limit(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> IntStream.of(1).skip(-1), "skip(-1) IAE");
        checkThrows(NullPointerException.class, () -> IntStream.of(1).map(null), "map(null) NPE");
        // findAny/noneMatch/allMatch
        check(IntStream.empty().findAny().isPresent() == false, "empty findAny empty");
        check(IntStream.range(0, 10).parallel().findAny().isPresent(), "parallel findAny present");
        check(IntStream.of(1, 2).noneMatch(i -> i > 5) && !IntStream.of(1, 2).noneMatch(i -> i > 1),
              "noneMatch both branches");
        // toArray 精确
        check(java.util.Arrays.equals(IntStream.range(0, 3).toArray(), new int[] {0, 1, 2}), "toArray content");
    }

    public static void main(String[] args) {
        testContracts();
        testFactories();
        testIntermediate();
        testTerminal();
        testToArrayAndReduce();
        testParallelConsistency();
        testSingleUse();
        testNullAndBadArgs();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
