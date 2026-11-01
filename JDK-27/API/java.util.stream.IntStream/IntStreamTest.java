/*
 * @test
 * @summary java.util.stream.IntStream API conformance: primitive stream ops.
 * @run main IntStreamTest
 */

import java.util.stream.IntStream;

public class IntStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIntStream() {
        check(IntStream.of(1, 2, 3).sum() == 6, "sum");
        check(IntStream.of(1, 2, 3).count() == 3, "count");
        check(IntStream.of(1, 2, 3).average().getAsDouble() == 2.0, "average");
        check(IntStream.of(1, 2, 3).max().getAsInt() == 3, "max");
        check(IntStream.of(1, 2, 3).min().getAsInt() == 1, "min");
        check(IntStream.range(1, 4).count() == 3, "range");
        check(IntStream.rangeClosed(1, 3).count() == 3, "rangeClosed");
        int[] sorted = IntStream.of(3, 1, 2).sorted().toArray();
        check(sorted[0] == 1 && sorted[2] == 3, "sorted/toArray");
        check(IntStream.of(1, 2, 3).map(x -> x * 2).sum() == 12, "map");
        check(IntStream.of(1, 2, 3).filter(x -> x > 1).count() == 2, "filter");
        check(IntStream.of(1, 2, 3).anyMatch(x -> x == 2), "anyMatch");
        check(IntStream.of(1, 2, 3).allMatch(x -> x > 0), "allMatch");
        check(IntStream.of(1, 2, 3).noneMatch(x -> x > 5), "noneMatch");
        check(IntStream.of(1, 1, 2).distinct().count() == 2, "distinct");
        check(IntStream.of(1, 2, 3).limit(2).count() == 2, "limit");
        check(IntStream.of(1, 2, 3).skip(1).findFirst().getAsInt() == 2, "skip/findFirst");
        check(IntStream.of(1, 2, 3).reduce(0, (a, b) -> a + b) == 6, "reduce");
        check(IntStream.of(1, 2, 3).boxed().count() == 3, "boxed");
        check(IntStream.of(1, 2, 3).mapToLong(x -> x).sum() == 6L, "mapToLong");
        check(IntStream.of(1, 2, 3).mapToDouble(x -> x).sum() == 6.0, "mapToDouble");
        check("1".equals(IntStream.of(1, 2).mapToObj(String::valueOf).findFirst().get()), "mapToObj");
        check(IntStream.of(1, 2, 3).parallel().isParallel(), "parallel");
        check(IntStream.concat(IntStream.of(1), IntStream.of(2)).sum() == 3, "concat");
        check(IntStream.empty().count() == 0, "empty");
    }

    public static void main(String[] args) {
        testIntStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
