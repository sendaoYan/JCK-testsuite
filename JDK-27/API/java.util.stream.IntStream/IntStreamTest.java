/*
 * @test
 * @summary java.util.stream.IntStream conformance: white-box structure (interface/BaseStream/
 *          member forms) + black-box behavior (range/rangeClosed, reductions, mapToObj/boxed,
 *          summaryStatistics).
 * @run main IntStreamTest
 */

import java.lang.reflect.Modifier;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IntStreamTest {
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
        Class<?> c = Class.forName("java.util.stream.IntStream");
        check(c.isInterface(), "IntStream is an interface");
        check(Modifier.isPublic(c.getModifiers()), "IntStream is public");
        check(c.getSuperclass() == null, "interface superclass is null");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0] == BaseStream.class,
              "IntStream extends BaseStream");

        check(hasMethod(c, "range", int.class, int.class), "IntStream#range(int,int)");
        check(hasMethod(c, "rangeClosed", int.class, int.class), "IntStream#rangeClosed(int,int)");
        check(hasMethod(c, "sum"), "IntStream#sum()");
        check(hasMethod(c, "summaryStatistics"), "IntStream#summaryStatistics()");
        check(hasMethod(c, "boxed"), "IntStream#boxed()");
        check(hasMethod(c, "mapToObj", IntFunction.class), "IntStream#mapToObj(IntFunction)");
        check(hasMethod(c, "filter", IntPredicate.class), "IntStream#filter(IntPredicate)");
    }

    // ===================== 黑盒 / 行为 =====================
    static void testRange() {
        int[] r = IntStream.range(1, 4).toArray();
        check(r.length == 3 && r[0] == 1 && r[2] == 3, "range half-open");
        int[] rc = IntStream.rangeClosed(1, 3).toArray();
        check(rc.length == 3 && rc[2] == 3, "rangeClosed inclusive");
        check(IntStream.range(5, 5).count() == 0, "range start==end empty");
        check(IntStream.range(6, 5).count() == 0, "range start>end empty");
        check(IntStream.rangeClosed(5, 5).sum() == 5, "rangeClosed single element");
        check(IntStream.rangeClosed(6, 5).count() == 0, "rangeClosed start>end empty");
    }

    static void testReductions() {
        check(IntStream.of(1, 2, 3, 4).sum() == 10, "sum");
        check(IntStream.of(1, 2, 3, 4).count() == 4, "count");
        check(IntStream.of(2, 4, 6).average().getAsDouble() == 4.0, "average");
        check(IntStream.of(3, 1, 2).min().getAsInt() == 1, "min");
        check(IntStream.of(3, 1, 2).max().getAsInt() == 3, "max");
        check(IntStream.empty().sum() == 0, "empty sum == 0");
        check(IntStream.empty().count() == 0, "empty count");
        check(!IntStream.empty().average().isPresent(), "empty average");
        check(!IntStream.empty().min().isPresent(), "empty min");
        check(!IntStream.empty().max().isPresent(), "empty max");
        check(IntStream.of(1, 2, 3).reduce(0, Integer::sum) == 6, "reduce identity");
        check(IntStream.of(1, 2, 3).reduce(Integer::sum).getAsInt() == 6, "reduce op");
        check(!IntStream.empty().reduce(Integer::sum).isPresent(), "empty reduce");
    }

    static void testIntermediateAndConversion() {
        int[] mapped = IntStream.range(1, 5).map(x -> x * x).toArray();
        check(mapped[0] == 1 && mapped[3] == 16, "map square");
        check(IntStream.range(1, 6).filter(x -> x % 2 == 0).sum() == 6, "filter even");
        check(IntStream.of(3, 1, 2).sorted().toArray()[0] == 1, "sorted");
        check(IntStream.of(1, 1, 2).distinct().count() == 2, "distinct");
        check(IntStream.of(1, 2, 3).limit(2).count() == 2, "limit");
        check(IntStream.of(1, 2, 3).skip(1).findFirst().getAsInt() == 2, "skip/findFirst");
        check(!IntStream.empty().findFirst().isPresent(), "empty findFirst");

        List<Integer> boxed = IntStream.of(1, 2, 3).boxed().collect(Collectors.toList());
        check(boxed.equals(List.of(1, 2, 3)), "boxed contents");
        List<String> obj = IntStream.range(1, 4).mapToObj(String::valueOf).collect(Collectors.toList());
        check(obj.equals(List.of("1", "2", "3")), "mapToObj");
        check(IntStream.of(1, 2, 3).mapToLong(x -> x * 2L).sum() == 12L, "mapToLong");
        check(IntStream.of(1, 2, 3).mapToDouble(x -> x * 2.0).sum() == 12.0, "mapToDouble");
        check(IntStream.range(1, 4).asLongStream().sum() == 6L, "asLongStream");
        check(IntStream.range(1, 4).asDoubleStream().sum() == 6.0, "asDoubleStream");
        check(IntStream.of(1, 2, 3).anyMatch(x -> x == 2), "anyMatch");
        check(IntStream.of(1, 2, 3).allMatch(x -> x > 0), "allMatch");
        check(IntStream.of(1, 2, 3).noneMatch(x -> x > 5), "noneMatch");
        check(IntStream.concat(IntStream.of(1), IntStream.of(2)).sum() == 3, "concat");
        check(IntStream.generate(() -> 7).limit(3).sum() == 21, "generate+limit");
        check(IntStream.iterate(1, x -> x + 1).limit(4).sum() == 10, "iterate+limit");
    }

    static void testSummaryStatistics() {
        IntSummaryStatistics stats = IntStream.of(1, 2, 3, 4).summaryStatistics();
        check(stats.getCount() == 4, "stats count");
        check(stats.getSum() == 10, "stats sum");
        check(stats.getMin() == 1, "stats min");
        check(stats.getMax() == 4, "stats max");
        check(stats.getAverage() == 2.5, "stats average");

        IntSummaryStatistics empty = IntStream.empty().summaryStatistics();
        check(empty.getCount() == 0 && empty.getSum() == 0, "empty stats count/sum");
        check(empty.getMin() == Integer.MAX_VALUE && empty.getMax() == Integer.MIN_VALUE,
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
