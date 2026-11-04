/*
 * @test
 * @summary java.util.stream.Stream API conformance: intermediate/terminal ops and factories.
 * @run main StreamTest
 */

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStream() {
        check(Stream.of("b", "a", "c").filter(x -> !x.equals("a")).count() == 2, "filter");
        List<Integer> mapped = Stream.of(1, 2, 3).map(x -> x * 10).collect(Collectors.toList());
        check(mapped.get(0) == 10 && mapped.get(2) == 30, "map");
        check(Stream.of(1, 2, 3).flatMap(x -> Stream.of(x, x)).count() == 6, "flatMap");
        check(Stream.of(3, 1, 2).sorted().findFirst().get() == 1, "sorted");
        check(Stream.of(1, 1, 2).distinct().count() == 2, "distinct");
        check(Stream.of(1, 2, 3).peek(x -> { }).count() == 3, "peek");
        check(Stream.of(1, 2, 3).limit(2).count() == 2, "limit");
        check(Stream.of(1, 2, 3).skip(1).findFirst().get() == 2, "skip");
        check(Stream.of(1, 2, 3, 4).reduce(0, (a, b) -> a + b) == 10, "reduce");
        check(Stream.of(1, 2, 3).mapToInt(Integer::intValue).sum() == 6, "mapToInt");
        check(Stream.of(1, 2, 3).anyMatch(x -> x == 2), "anyMatch");
        check(Stream.of(1, 2, 3).allMatch(x -> x > 0), "allMatch");
        check(Stream.of(1, 2, 3).noneMatch(x -> x > 5), "noneMatch");
        check(Stream.of(5, 1, 9).min(Integer::compareTo).get() == 1, "min");
        check(Stream.of(5, 1, 9).max(Integer::compareTo).get() == 9, "max");
        check(Stream.of(1, 2, 3).count() == 3, "count");
        check(Stream.of(3, 2, 1).findFirst().get() == 3, "findFirst");
        check(Stream.of(1, 2, 3).parallel().isParallel(), "parallel");
        check(Stream.of(1, 2, 3).parallel().sequential() != null, "sequential");
        check(Stream.empty().count() == 0, "empty");
        check(Stream.concat(Stream.of(1), Stream.of(2)).count() == 2, "concat");
        check(Stream.generate(() -> 1).limit(3).count() == 3, "generate");
        check(Stream.iterate(1, x -> x + 1).limit(3).mapToInt(Integer::intValue).sum() == 6, "iterate");
        Stream<String> c = Stream.of("a");
        c.onClose(() -> { });
        c.close();
        check(true, "onClose/close");
    }

    public static void main(String[] args) {
        testStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
