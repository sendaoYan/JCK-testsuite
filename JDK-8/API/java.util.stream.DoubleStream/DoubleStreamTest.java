/*
 * @test
 * @summary java.util.stream.DoubleStream API conformance: primitive stream ops.
 * @run main DoubleStreamTest
 */

import java.util.stream.DoubleStream;

public class DoubleStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDoubleStream() {
        check(DoubleStream.of(1.0, 2.0, 3.0).sum() == 6.0, "sum");
        check(DoubleStream.of(1.0, 2.0, 3.0).count() == 3L, "count");
        check(DoubleStream.of(1.0, 2.0, 3.0).average().getAsDouble() == 2.0, "average");
        check(DoubleStream.of(1.0, 2.0, 3.0).max().getAsDouble() == 3.0, "max");
        check(DoubleStream.of(1.0, 2.0, 3.0).min().getAsDouble() == 1.0, "min");
        double[] sorted = DoubleStream.of(3.0, 1.0, 2.0).sorted().toArray();
        check(sorted[0] == 1.0 && sorted[2] == 3.0, "sorted/toArray");
        check(DoubleStream.of(1.0, 2.0, 3.0).map(x -> x * 2.0).sum() == 12.0, "map");
        check(DoubleStream.of(1.0, 2.0, 3.0).filter(x -> x > 1.0).count() == 2L, "filter");
        check(DoubleStream.of(1.0, 2.0, 3.0).anyMatch(x -> x == 2.0), "anyMatch");
        check(DoubleStream.of(1.0, 2.0, 3.0).allMatch(x -> x > 0.0), "allMatch");
        check(DoubleStream.of(1.0, 2.0, 3.0).noneMatch(x -> x > 5.0), "noneMatch");
        check(DoubleStream.of(1.0, 1.0, 2.0).distinct().count() == 2L, "distinct");
        check(DoubleStream.of(1.0, 2.0, 3.0).limit(2).count() == 2L, "limit");
        check(DoubleStream.of(1.0, 2.0, 3.0).skip(1).findFirst().getAsDouble() == 2.0, "skip/findFirst");
        check(DoubleStream.of(1.0, 2.0, 3.0).reduce(0.0, (a, b) -> a + b) == 6.0, "reduce");
        check(DoubleStream.of(1.0, 2.0, 3.0).boxed().count() == 3L, "boxed");
        check(DoubleStream.of(1.0, 2.0, 3.0).mapToLong(x -> (long) x).sum() == 6L, "mapToLong");
        check(DoubleStream.of(1.0, 2.0, 3.0).mapToInt(x -> (int) x).sum() == 6, "mapToInt");
        check(DoubleStream.concat(DoubleStream.of(1.0), DoubleStream.of(2.0)).sum() == 3.0, "concat");
        check(DoubleStream.empty().count() == 0L, "empty");
    }

    public static void main(String[] args) {
        testDoubleStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
