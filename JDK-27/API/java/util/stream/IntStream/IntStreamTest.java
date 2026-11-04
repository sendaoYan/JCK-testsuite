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

    static void testJava9Plus() {
        int[] it = IntStream.iterate(1, x -> x < 5, x -> x + 1).toArray();
        check(Arrays.equals(it, new int[]{1, 2, 3, 4}), "iterate(seed, hasNext, next)");
        check(Arrays.equals(IntStream.of(1, 2, 3, 4).takeWhile(x -> x < 3).toArray(), new int[]{1, 2}), "takeWhile");
        check(Arrays.equals(IntStream.of(1, 2, 3, 4).dropWhile(x -> x < 3).toArray(), new int[]{3, 4}), "dropWhile");
        check(IntStream.of(1, 2, 3).mapMulti((x, sink) -> { sink.accept(x); sink.accept(x); }).sum() == 12, "mapMulti");
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

    public static void main(String[] args) {
        testFactories();
        testIntermediate();
        testTerminal();
        testToArrayAndReduce();
        testParallelConsistency();
        testSingleUse();
        testNullAndBadArgs();
        testJava9Plus();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
