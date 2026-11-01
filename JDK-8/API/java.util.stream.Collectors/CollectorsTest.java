/*
 * @test
 * @summary java.util.stream.Collectors API conformance: common collectors.
 * @run main CollectorsTest
 */

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectorsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCollectors() {
        List<Integer> list = Stream.of(1, 2, 3).collect(Collectors.toList());
        check(list.size() == 3, "toList");
        Set<Integer> set = Stream.of(1, 1, 2).collect(Collectors.toSet());
        check(set.size() == 2, "toSet");
        List<Integer> col = Stream.of(1, 2).collect(Collectors.toCollection(ArrayList::new));
        check(col.size() == 2, "toCollection");
        check("a-b".equals(Stream.of("a", "b").collect(Collectors.joining("-"))), "joining");
        check(Stream.of("x", "y").collect(Collectors.counting()) == 2L, "counting");
        check(Stream.of(1, 2, 3).collect(Collectors.summingInt(Integer::intValue)) == 6, "summingInt");
        check(Stream.of(1L, 2L).collect(Collectors.summingLong(Long::longValue)) == 3L, "summingLong");
        check(Stream.of(1.0, 2.0).collect(Collectors.summingDouble(Double::doubleValue)) == 3.0, "summingDouble");
        check(Stream.of(1, 2, 3).collect(Collectors.averagingInt(Integer::intValue)) == 2.0, "averagingInt");
        Map<Boolean, List<Integer>> part = Stream.of(1, 2, 3).collect(Collectors.partitioningBy(x -> x > 1));
        check(part.get(true).size() == 2 && part.get(false).size() == 1, "partitioningBy");
        Map<Integer, List<Integer>> group = Stream.of(1, 2, 3).collect(Collectors.groupingBy(x -> x % 2));
        check(group.get(0).size() == 1 && group.get(1).size() == 2, "groupingBy");
        check(Stream.of(1, 9, 3).collect(Collectors.maxBy(Integer::compareTo)).get() == 9, "maxBy");
        check(Stream.of(1, 9, 3).collect(Collectors.minBy(Integer::compareTo)).get() == 1, "minBy");
        Map<Integer, Integer> toMap = Stream.of("a", "bb", "ccc").collect(
            Collectors.toMap(String::length, s -> s.length()));
        check(toMap.get(1) == 1 && toMap.get(3) == 3, "toMap");
        IntSummaryStatistics stats = Stream.of(1, 2, 3).collect(Collectors.summarizingInt(Integer::intValue));
        check(stats.getSum() == 6 && stats.getCount() == 3, "summarizingInt");
        check("AB".equals(Stream.of("a", "b").collect(
            Collectors.mapping(String::toUpperCase, Collectors.joining()))), "mapping");
        check(Stream.of(1, 2, 3).collect(
            Collectors.reducing(0, Integer::intValue, Integer::sum)) == 6, "reducing");
    }

    public static void main(String[] args) {
        testCollectors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
