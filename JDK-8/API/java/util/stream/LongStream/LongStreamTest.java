/*
 * @test
 * @summary java.util.stream.LongStream API conformance: primitive stream ops.
 * @run main LongStreamTest
 */

import java.util.stream.LongStream;

public class LongStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLongStream() {
        check(LongStream.of(1L, 2L, 3L).sum() == 6L, "sum");
        check(LongStream.of(1L, 2L, 3L).count() == 3L, "count");
        check(LongStream.of(1L, 2L, 3L).average().getAsDouble() == 2.0, "average");
        check(LongStream.of(1L, 2L, 3L).max().getAsLong() == 3L, "max");
        check(LongStream.of(1L, 2L, 3L).min().getAsLong() == 1L, "min");
        check(LongStream.range(1L, 4L).count() == 3L, "range");
        check(LongStream.rangeClosed(1L, 3L).count() == 3L, "rangeClosed");
        long[] sorted = LongStream.of(3L, 1L, 2L).sorted().toArray();
        check(sorted[0] == 1L && sorted[2] == 3L, "sorted/toArray");
        check(LongStream.of(1L, 2L, 3L).map(x -> x * 2L).sum() == 12L, "map");
        check(LongStream.of(1L, 2L, 3L).filter(x -> x > 1L).count() == 2L, "filter");
        check(LongStream.of(1L, 2L, 3L).anyMatch(x -> x == 2L), "anyMatch");
        check(LongStream.of(1L, 2L, 3L).allMatch(x -> x > 0L), "allMatch");
        check(LongStream.of(1L, 2L, 3L).noneMatch(x -> x > 5L), "noneMatch");
        check(LongStream.of(1L, 1L, 2L).distinct().count() == 2L, "distinct");
        check(LongStream.of(1L, 2L, 3L).limit(2).count() == 2L, "limit");
        check(LongStream.of(1L, 2L, 3L).skip(1).findFirst().getAsLong() == 2L, "skip/findFirst");
        check(LongStream.of(1L, 2L, 3L).reduce(0L, (a, b) -> a + b) == 6L, "reduce");
        check(LongStream.of(1L, 2L, 3L).boxed().count() == 3L, "boxed");
        check(LongStream.of(1L, 2L, 3L).mapToDouble(x -> x).sum() == 6.0, "mapToDouble");
        check(LongStream.of(1L, 2L, 3L).mapToInt(x -> (int) x).sum() == 6, "mapToInt");
        check(LongStream.concat(LongStream.of(1L), LongStream.of(2L)).sum() == 3L, "concat");
        check(LongStream.empty().count() == 0L, "empty");
    }

    public static void main(String[] args) {
        testLongStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
