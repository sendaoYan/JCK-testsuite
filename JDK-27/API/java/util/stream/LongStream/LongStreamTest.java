/*
 * @test
 * @summary java.util.stream.LongStream contract: factories, intermediate/terminal ops,
 *          empty-stream semantics, parallel consistency, null->NPE, bad-arg->IAE,
 *          boxed/asDouble, single-use.
 * @run main LongStreamTest
 */

import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class LongStreamTest {
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
        check(LongStream.empty().count() == 0, "empty");
        check(LongStream.of(1L, 2L, 3L).sum() == 6L, "of varargs");
        check(LongStream.concat(LongStream.of(1L), LongStream.of(2L, 3L)).sum() == 6L, "concat");
        check(LongStream.generate(() -> 1L).limit(3).sum() == 3L, "generate");
        check(LongStream.iterate(1L, x -> x + 1).limit(4).sum() == 10L, "iterate");
        long[] ranged = LongStream.range(1L, 5L).toArray();
        check(Arrays.equals(ranged, new long[]{1L, 2L, 3L, 4L}), "range exclusive");
        long[] closed = LongStream.rangeClosed(1L, 4L).toArray();
        check(Arrays.equals(closed, new long[]{1L, 2L, 3L, 4L}), "rangeClosed inclusive");
    }

    static void testIntermediate() {
        check(LongStream.of(1L, 2L, 3L).map(x -> x * 10L).sum() == 60L, "map");
        check(LongStream.of(1L, 2L, 3L, 4L).filter(x -> x % 2 == 0).sum() == 6L, "filter");
        check(LongStream.of(1L, 2L).flatMap(x -> LongStream.of(x, x)).sum() == 6L, "flatMap");
        check(LongStream.of(3L, 1L, 2L).sorted().toArray()[0] == 1L, "sorted");
        check(LongStream.of(1L, 1L, 2L).distinct().count() == 2, "distinct");
        check(LongStream.of(1L, 2L, 3L, 4L).limit(2).sum() == 3L, "limit");
        check(LongStream.of(1L, 2L, 3L, 4L).skip(2).sum() == 7L, "skip");
        List<Long> boxed = LongStream.of(1L, 2L, 3L).boxed().collect(Collectors.toList());
        check(boxed.equals(Arrays.asList(1L, 2L, 3L)), "boxed");
        check(LongStream.of(1L, 2L).asDoubleStream().sum() == 3.0, "asDoubleStream");
        check(LongStream.of(1L, 2L, 3L).mapToObj(Long::valueOf).count() == 3, "mapToObj");
        check(LongStream.of(1L, 2L, 3L).mapToInt(x -> (int) x).sum() == 6, "mapToInt");
        check(LongStream.of(1L, 2L, 3L).mapToDouble(x -> x * 2.0).sum() == 12.0, "mapToDouble");
    }

    static void testTerminal() {
        check(LongStream.of(1L, 2L, 3L, 4L).sum() == 10L, "sum");
        check(LongStream.empty().sum() == 0L, "empty sum 0");
        check(LongStream.of(5L, 1L, 9L).min().getAsLong() == 1L, "min");
        check(LongStream.of(5L, 1L, 9L).max().getAsLong() == 9L, "max");
        check(!LongStream.empty().min().isPresent(), "empty min");
        OptionalDouble avg = LongStream.of(1L, 2L, 3L, 4L).average();
        check(avg.getAsDouble() == 2.5, "average");
        check(!LongStream.empty().average().isPresent(), "empty average");
        check(LongStream.of(1L, 2L, 3L).count() == 3, "count");
        check(LongStream.of(1L, 2L, 3L).reduce(0L, (a, b) -> a + b) == 6L, "reduce identity");
        check(LongStream.of(1L, 2L, 3L).reduce((a, b) -> a + b).getAsLong() == 6L, "reduce no identity");
        java.util.LongSummaryStatistics st = LongStream.of(1L, 2L, 3L, 4L).summaryStatistics();
        check(st.getCount() == 4 && st.getSum() == 10L && st.getMin() == 1L && st.getMax() == 4L
                && st.getAverage() == 2.5, "summaryStatistics");
        check(LongStream.of(1L, 2L, 3L).anyMatch(x -> x == 2L), "anyMatch");
        check(LongStream.of(1L, 2L, 3L).allMatch(x -> x > 0L), "allMatch");
        check(LongStream.of(1L, 2L, 3L).noneMatch(x -> x > 5L), "noneMatch");
        check(LongStream.of(3L, 2L, 1L).findFirst().getAsLong() == 3L, "findFirst");
        check(LongStream.of(1L, 2L, 3L).findAny().isPresent(), "findAny");
    }

    static void testParallelConsistency() {
        long serial = LongStream.range(1, 100).sum();
        long parallel = LongStream.range(1, 100).parallel().sum();
        check(serial == parallel, "parallel sum equals serial");
    }

    static void testSingleUse() {
        LongStream s = LongStream.of(1L, 2L, 3L);
        check(s.sum() == 6L, "first consumption");
        checkThrows(IllegalStateException.class, () -> s.sum(), "consumed twice ISE");
    }

    static void testJava9Plus() {
        long[] it = LongStream.iterate(1L, x -> x < 5L, x -> x + 1L).toArray();
        check(Arrays.equals(it, new long[]{1L, 2L, 3L, 4L}), "iterate(seed, hasNext, next)");
        check(Arrays.equals(LongStream.of(1L, 2L, 3L, 4L).takeWhile(x -> x < 3L).toArray(), new long[]{1L, 2L}), "takeWhile");
        check(Arrays.equals(LongStream.of(1L, 2L, 3L, 4L).dropWhile(x -> x < 3L).toArray(), new long[]{3L, 4L}), "dropWhile");
    }

    static void testNullAndBadArgs() {
        checkThrows(NullPointerException.class, () -> LongStream.of(1L).filter(null), "filter(null)");
        checkThrows(NullPointerException.class, () -> LongStream.of(1L).map(null), "map(null)");
        checkThrows(IllegalArgumentException.class, () -> LongStream.of(1L).limit(-1), "limit(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> LongStream.of(1L).skip(-1), "skip(-1) IAE");
        checkThrows(NullPointerException.class, () -> LongStream.of(1L).reduce((java.util.function.LongBinaryOperator) null), "reduce null accumulator NPE");
        checkThrows(NullPointerException.class, () -> LongStream.of(1L).mapToObj(null), "mapToObj(null)");
    }


    // ===================== 契约深化：range 边界/Gauss 大数/溢出语义/统计/boxed/asDoubleStream =====================
    static void testContracts() {
        check(LongStream.range(1, 5).count() == 4, "range excludes end");
        check(LongStream.rangeClosed(1, 5).count() == 5, "rangeClosed includes end");
        // Gauss 1..10^6（顺序 = 并行 = 公式）
        long n = 1_000_000L;
        long serial = LongStream.rangeClosed(1, n).sum();
        long parallel = LongStream.rangeClosed(1, n).parallel().sum();
        check(serial == n * (n + 1) / 2 && parallel == serial, "gauss 1e6 serial==parallel==formula");
        // int 域越界求和 long 不溢出
        check(LongStream.rangeClosed(2_000_000_000L, 2_000_000_002L).sum() == 6_000_000_003L,
              "sum beyond int range exact");
        // 统计
        java.util.LongSummaryStatistics st = LongStream.of(5L, 1L, 3L).summaryStatistics();
        check(st.getCount() == 3 && st.getSum() == 9L && st.getMin() == 1L && st.getMax() == 5L
              && st.getAverage() == 3.0, "LongSummaryStatistics all fields");
        check(LongStream.empty().sum() == 0L && !LongStream.empty().average().isPresent(),
              "empty stats contracts");
        // boxed/asDoubleStream/mapToObj
        check(LongStream.of(1L, 2L).boxed().collect(Collectors.toList()).equals(Arrays.asList(1L, 2L)), "boxed");
        check(LongStream.of(1L, 2L).asDoubleStream().average().getAsDouble() == 1.5, "asDoubleStream");
        check(LongStream.of(1L, 2L).mapToObj(String::valueOf).collect(Collectors.toList())
              .equals(Arrays.asList("1", "2")), "mapToObj");
        // distinct/sorted/concat
        check(java.util.Arrays.equals(LongStream.of(3, 1, 1, 2).sorted().distinct().toArray(),
              new long[] {1, 2, 3}), "sorted+distinct");
        check(LongStream.concat(LongStream.of(1), LongStream.of(2)).sum() == 3L, "concat");
        // iterate + limit
        check(java.util.Arrays.equals(LongStream.iterate(1L, i -> i + 1).limit(4).toArray(),
              new long[] {1, 2, 3, 4}), "iterate bounded");
        checkThrows(IllegalArgumentException.class, () -> LongStream.of(1).limit(-1), "limit(-1) IAE");
        checkThrows(NullPointerException.class, () -> LongStream.of(1).map(null), "map(null) NPE");
        // parallel range 全等
        check(java.util.Arrays.equals(LongStream.range(0, 50).parallel().sorted().toArray(),
              LongStream.range(0, 50).sorted().toArray()), "parallel sorted == sequential");
    }


    // ===================== JDK-9+/16+ 增量：takeWhile/dropWhile/mapMulti/iterate 三参（JDK-27 侧锁定） =====================
    static void testJdk27Additions() {
        check(LongStream.of(1, 2, 3, 1).takeWhile(i -> i < 3).count() == 2, "takeWhile prefix");
        check(LongStream.of(1, 2, 3, 1).dropWhile(i -> i < 3).count() == 2, "dropWhile rest");
        check(java.util.Arrays.equals(LongStream.of(1, 2).mapMulti((i, sink) -> { sink.accept(i); sink.accept(i * 10); }).toArray(),
              new long[] {1, 10, 2, 20}), "mapMulti expands (LongConsumer sink keeps primitive)");
        check(LongStream.iterate(1, i -> i <= 8, i -> i * 2).sum() == 15, "iterate 3-arg");
    }

    public static void main(String[] args) {
        testJdk27Additions();
        testContracts();
        testFactories();
        testIntermediate();
        testTerminal();
        testParallelConsistency();
        testSingleUse();
        testNullAndBadArgs();
        testJava9Plus();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
