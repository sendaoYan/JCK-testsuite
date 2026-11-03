/*
 * @test
 * @summary java.util.stream.DoubleStream conformance: white-box structure (interface/BaseStream/
 *          member forms) + black-box behavior (reductions, mapToObj/boxed, summaryStatistics).
 * @run main DoubleStreamTest
 */

import java.lang.reflect.Modifier;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class DoubleStreamTest {
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
        Class<?> c = Class.forName("java.util.stream.DoubleStream");
        check(c.isInterface(), "DoubleStream is an interface");
        check(Modifier.isPublic(c.getModifiers()), "DoubleStream is public");
        check(c.getSuperclass() == null, "interface superclass is null");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0] == BaseStream.class,
              "DoubleStream extends BaseStream");

        check(hasMethod(c, "sum"), "DoubleStream#sum()");
        check(hasMethod(c, "summaryStatistics"), "DoubleStream#summaryStatistics()");
        check(hasMethod(c, "boxed"), "DoubleStream#boxed()");
        check(hasMethod(c, "mapToObj", DoubleFunction.class), "DoubleStream#mapToObj(DoubleFunction)");
        check(hasMethod(c, "filter", DoublePredicate.class), "DoubleStream#filter(DoublePredicate)");
        check(hasMethod(c, "mapToInt", java.util.function.DoubleToIntFunction.class),
              "DoubleStream#mapToInt(DoubleToIntFunction)");
    }

    // ===================== 黑盒 / 行为 =====================
    static void testReductions() {
        check(DoubleStream.of(1.0, 2.0, 3.0, 4.0).sum() == 10.0, "sum");
        check(DoubleStream.of(1.0, 2.0, 3.0, 4.0).count() == 4L, "count");
        check(DoubleStream.of(2.0, 4.0, 6.0).average().getAsDouble() == 4.0, "average");
        check(DoubleStream.of(3.0, 1.0, 2.0).min().getAsDouble() == 1.0, "min");
        check(DoubleStream.of(3.0, 1.0, 2.0).max().getAsDouble() == 3.0, "max");
        check(DoubleStream.empty().sum() == 0.0, "empty sum == 0");
        check(DoubleStream.empty().count() == 0L, "empty count");
        check(!DoubleStream.empty().average().isPresent(), "empty average");
        check(!DoubleStream.empty().min().isPresent(), "empty min");
        check(!DoubleStream.empty().max().isPresent(), "empty max");
        check(DoubleStream.of(1.0, 2.0, 3.0).reduce(0.0, Double::sum) == 6.0, "reduce identity");
        check(DoubleStream.of(1.0, 2.0, 3.0).reduce(Double::sum).getAsDouble() == 6.0, "reduce op");
        check(!DoubleStream.empty().reduce(Double::sum).isPresent(), "empty reduce");
    }

    static void testIntermediateAndConversion() {
        double[] mapped = DoubleStream.of(1.0, 2.0, 3.0, 4.0).map(x -> x * x).toArray();
        check(mapped[0] == 1.0 && mapped[3] == 16.0, "map square");
        check(DoubleStream.of(1.0, 2.0, 3.0, 4.0).filter(x -> x % 2.0 == 0.0).sum() == 6.0, "filter even");
        check(DoubleStream.of(3.0, 1.0, 2.0).sorted().toArray()[0] == 1.0, "sorted");
        check(DoubleStream.of(1.0, 1.0, 2.0).distinct().count() == 2L, "distinct");
        check(DoubleStream.of(1.0, 2.0, 3.0).limit(2).count() == 2L, "limit");
        check(DoubleStream.of(1.0, 2.0, 3.0).skip(1).findFirst().getAsDouble() == 2.0, "skip/findFirst");
        check(!DoubleStream.empty().findFirst().isPresent(), "empty findFirst");

        List<Double> boxed = DoubleStream.of(1.0, 2.0, 3.0).boxed().collect(Collectors.toList());
        check(boxed.equals(List.of(1.0, 2.0, 3.0)), "boxed contents");
        List<String> obj = DoubleStream.of(1.0, 2.0, 3.0).mapToObj(String::valueOf)
            .collect(Collectors.toList());
        check(obj.equals(List.of("1.0", "2.0", "3.0")), "mapToObj");
        check(DoubleStream.of(1.5, 2.5, 3.5).mapToInt(x -> (int) x).sum() == 6, "mapToInt");
        check(DoubleStream.of(1.0, 2.0, 3.0).mapToLong(x -> (long) x * 2L).sum() == 12L, "mapToLong");
        check(DoubleStream.of(1.0, 2.0, 3.0).anyMatch(x -> x == 2.0), "anyMatch");
        check(DoubleStream.of(1.0, 2.0, 3.0).allMatch(x -> x > 0.0), "allMatch");
        check(DoubleStream.of(1.0, 2.0, 3.0).noneMatch(x -> x > 5.0), "noneMatch");
        check(DoubleStream.concat(DoubleStream.of(1.0), DoubleStream.of(2.0)).sum() == 3.0, "concat");
        check(DoubleStream.generate(() -> 7.0).limit(3).sum() == 21.0, "generate+limit");
        check(DoubleStream.iterate(1.0, x -> x + 1.0).limit(4).sum() == 10.0, "iterate+limit");

        DoubleStream.Builder b = DoubleStream.builder();
        b.add(1.0).add(2.0).add(3.0);
        check(b.build().sum() == 6.0, "builder");
    }

    static void testSummaryStatistics() {
        DoubleSummaryStatistics stats = DoubleStream.of(1.0, 2.0, 3.0, 4.0).summaryStatistics();
        check(stats.getCount() == 4L, "stats count");
        check(stats.getSum() == 10.0, "stats sum");
        check(stats.getMin() == 1.0, "stats min");
        check(stats.getMax() == 4.0, "stats max");
        check(stats.getAverage() == 2.5, "stats average");

        DoubleSummaryStatistics empty = DoubleStream.empty().summaryStatistics();
        check(empty.getCount() == 0L && empty.getSum() == 0.0, "empty stats count/sum");
        check(empty.getMin() == Double.POSITIVE_INFINITY && empty.getMax() == Double.NEGATIVE_INFINITY,
              "empty stats min/max sentinel");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testReductions();
        testIntermediateAndConversion();
        testSummaryStatistics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
