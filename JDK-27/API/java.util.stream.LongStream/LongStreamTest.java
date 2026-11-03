/*
 * @test
 * @summary java.util.stream.LongStream conformance: white-box structure (interface/BaseStream/
 *          member forms) + black-box behavior (range/rangeClosed, reductions, mapToObj/boxed,
 *          summaryStatistics).
 * @run main LongStreamTest
 */

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class LongStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ===================== 白盒 / 结构（反射） =====================
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.stream.LongStream");
        check(c.isInterface(), "LongStream is an interface");
        check(Modifier.isPublic(c.getModifiers()), "LongStream is public");
        check(c.getSuperclass() == null, "interface superclass is null");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0] == BaseStream.class,
              "LongStream extends BaseStream");

        check(hasMethod(c, "range", long.class, long.class), "LongStream#range(long,long)");
        check(hasMethod(c, "rangeClosed", long.class, long.class), "LongStream#rangeClosed(long,long)");
        check(hasMethod(c, "sum"), "LongStream#sum()");
        check(hasMethod(c, "summaryStatistics"), "LongStream#summaryStatistics()");
        check(hasMethod(c, "boxed"), "LongStream#boxed()");
        check(hasMethod(c, "mapToObj", LongFunction.class), "LongStream#mapToObj(LongFunction)");
        check(hasMethod(c, "filter", LongPredicate.class), "LongStream#filter(LongPredicate)");
    }

    // ===================== 黑盒 / 行为 =====================
    static void testRange() {
        long[] r = LongStream.range(1L, 4L).toArray();
        check(r.length == 3 && r[0] == 1L && r[2] == 3L, "range half-open");
        long[] rc = LongStream.rangeClosed(1L, 3L).toArray();
        check(rc.length == 3 && rc[2] == 3L, "rangeClosed inclusive");
        check(LongStream.range(5L, 5L).count() == 0L, "range start==end empty");
        check(LongStream.range(6L, 5L).count() == 0L, "range start>end empty");
        check(LongStream.rangeClosed(5L, 5L).sum() == 5L, "rangeClosed single element");
        check(LongStream.rangeClosed(6L, 5L).count() == 0L, "rangeClosed start>end empty");
    }

    static void testReductions() {
        check(LongStream.of(1L, 2L, 3L, 4L).sum() == 10L, "sum");
        check(LongStream.of(1L, 2L, 3L, 4L).count() == 4L, "count");
        check(LongStream.of(2L, 4L, 6L).average().getAsDouble() == 4.0, "average");
        check(LongStream.of(3L, 1L, 2L).min().getAsLong() == 1L, "min");
        check(LongStream.of(3L, 1L, 2L).max().getAsLong() == 3L, "max");
        check(LongStream.empty().sum() == 0L, "empty sum == 0");
        check(LongStream.empty().count() == 0L, "empty count");
        check(!LongStream.empty().average().isPresent(), "empty average");
        check(!LongStream.empty().min().isPresent(), "empty min");
        check(!LongStream.empty().max().isPresent(), "empty max");
        check(LongStream.of(1L, 2L, 3L).reduce(0L, Long::sum) == 6L, "reduce identity");
        check(LongStream.of(1L, 2L, 3L).reduce(Long::sum).getAsLong() == 6L, "reduce op");
        check(!LongStream.empty().reduce(Long::sum).isPresent(), "empty reduce");
    }

    static void testIntermediateAndConversion() {
        long[] mapped = LongStream.range(1L, 5L).map(x -> x * x).toArray();
        check(mapped[0] == 1L && mapped[3] == 16L, "map square");
        check(LongStream.range(1L, 6L).filter(x -> x % 2L == 0L).sum() == 6L, "filter even");
        check(LongStream.of(3L, 1L, 2L).sorted().toArray()[0] == 1L, "sorted");
        check(LongStream.of(1L, 1L, 2L).distinct().count() == 2L, "distinct");
        check(LongStream.of(1L, 2L, 3L).limit(2).count() == 2L, "limit");
        check(LongStream.of(1L, 2L, 3L).skip(1).findFirst().getAsLong() == 2L, "skip/findFirst");
        check(!LongStream.empty().findFirst().isPresent(), "empty findFirst");

        List<Long> boxed = LongStream.of(1L, 2L, 3L).boxed().collect(Collectors.toList());
        check(boxed.equals(List.of(1L, 2L, 3L)), "boxed contents");
        List<String> obj = LongStream.range(1L, 4L).mapToObj(String::valueOf).collect(Collectors.toList());
        check(obj.equals(List.of("1", "2", "3")), "mapToObj");
        check(LongStream.of(1L, 2L, 3L).mapToInt(x -> (int) x * 2).sum() == 12, "mapToInt");
        check(LongStream.of(1L, 2L, 3L).mapToDouble(x -> x * 2.0).sum() == 12.0, "mapToDouble");
        check(LongStream.range(1L, 4L).asDoubleStream().sum() == 6.0, "asDoubleStream");
        check(LongStream.of(1L, 2L, 3L).anyMatch(x -> x == 2L), "anyMatch");
        check(LongStream.of(1L, 2L, 3L).allMatch(x -> x > 0L), "allMatch");
        check(LongStream.of(1L, 2L, 3L).noneMatch(x -> x > 5L), "noneMatch");
        check(LongStream.concat(LongStream.of(1L), LongStream.of(2L)).sum() == 3L, "concat");
        check(LongStream.generate(() -> 7L).limit(3).sum() == 21L, "generate+limit");
        check(LongStream.iterate(1L, x -> x + 1L).limit(4).sum() == 10L, "iterate+limit");
    }

    static void testSummaryStatistics() {
        LongSummaryStatistics stats = LongStream.of(1L, 2L, 3L, 4L).summaryStatistics();
        check(stats.getCount() == 4L, "stats count");
        check(stats.getSum() == 10L, "stats sum");
        check(stats.getMin() == 1L, "stats min");
        check(stats.getMax() == 4L, "stats max");
        check(stats.getAverage() == 2.5, "stats average");

        LongSummaryStatistics empty = LongStream.empty().summaryStatistics();
        check(empty.getCount() == 0L && empty.getSum() == 0L, "empty stats count/sum");
        check(empty.getMin() == Long.MAX_VALUE && empty.getMax() == Long.MIN_VALUE,
              "empty stats min/max sentinel");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testRange();
        testReductions();
        testIntermediateAndConversion();
        testSummaryStatistics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
