/*
 * @test
 * @summary java.util.stream.Collectors contract: toList/toSet/toCollection/joining,
 *          mapping/collectingAndThen/counting/minBy/maxBy/summing/averaging/
 *          reducing/groupingBy/partitioningBy/toMap/toConcurrentMap/summarizing,
 *          empty-stream semantics, null->NPE for every arg, duplicate key -> ISE.
 * @run main CollectorsTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectorsTest {
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

    static void testToCollectionAndList() {
        check(Stream.of(1, 2, 3).collect(Collectors.toList()).equals(Arrays.asList(1, 2, 3)), "toList");
        check(Stream.<Integer>empty().collect(Collectors.toList()).isEmpty(), "toList empty");
        check(Stream.of(1, 1, 2).collect(Collectors.toSet()).equals(new java.util.HashSet<>(Arrays.asList(1, 2))), "toSet");
        ArrayList<Integer> al = Stream.of(1, 2).collect(Collectors.toCollection(ArrayList::new));
        check(al instanceof ArrayList && al.size() == 2, "toCollection type");
        TreeSet<Integer> ts = Stream.of(3, 1, 2).collect(Collectors.toCollection(TreeSet::new));
        check(ts.first() == 1 && ts.last() == 3, "toCollection TreeSet");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.toCollection(null)), "toCollection(null) NPE");
    }

    static void testJoining() {
        check("abc".equals(Stream.of("a", "b", "c").collect(Collectors.joining())), "joining no args");
        check("a-b-c".equals(Stream.of("a", "b", "c").collect(Collectors.joining("-"))), "joining delimiter");
        check("[a,b]".equals(Stream.of("a", "b").collect(Collectors.joining(",", "[", "]"))), "joining prefix suffix");
        check("".equals(Stream.<String>empty().collect(Collectors.joining("-"))), "joining empty");
        checkThrows(NullPointerException.class, () -> Stream.of("a").collect(Collectors.joining(null)), "joining(null) NPE");
        checkThrows(NullPointerException.class, () -> Stream.of("a").collect(Collectors.joining(",", null, "]")), "joining null prefix NPE");
        checkThrows(NullPointerException.class, () -> Stream.of("a").collect(Collectors.joining(",", "[", null)), "joining null suffix NPE");
    }

    static void testMappingCollectingAndThen() {
        check("AB".equals(Stream.of("a", "b").collect(Collectors.mapping(String::toUpperCase, Collectors.joining()))), "mapping");
        check(Stream.of(1, 2, 3).collect(Collectors.mapping(String::valueOf, Collectors.toList()))
                .equals(Arrays.asList("1", "2", "3")), "mapping toList");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.mapping(null, Collectors.toList())), "mapping(null,..) NPE");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.mapping(Function.identity(), null)), "mapping(..,null) NPE");
        int wrapped = Stream.of(1, 2, 3).collect(Collectors.collectingAndThen(Collectors.toList(), List::size));
        check(wrapped == 3, "collectingAndThen");
        checkThrows(NullPointerException.class,
                () -> Stream.of(1).collect(Collectors.collectingAndThen(
                        (java.util.stream.Collector<Integer, ?, List<Integer>>) null, List::size)),
                "collectingAndThen(null,..) NPE");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.collectingAndThen(Collectors.toList(), null)), "collectingAndThen(..,null) NPE");
    }

    static void testCountingMinMaxSumming() {
        check(Stream.of(1, 2, 3).collect(Collectors.counting()) == 3L, "counting");
        check(Stream.empty().collect(Collectors.counting()) == 0L, "counting empty");
        check(Stream.of(1, 2, 3).collect(Collectors.summingInt(Integer::intValue)) == 6, "summingInt");
        check(Stream.<Integer>empty().collect(Collectors.summingInt(Integer::intValue)) == 0, "summingInt empty");
        check(Stream.of(1L, 2L).collect(Collectors.summingLong(Long::longValue)) == 3L, "summingLong");
        check(Stream.of(1.0, 2.5).collect(Collectors.summingDouble(Double::doubleValue)) == 3.5, "summingDouble");
        check(Stream.of(1, 2, 3).collect(Collectors.averagingInt(Integer::intValue)) == 2.0, "averagingInt");
        check(Stream.<Integer>empty().collect(Collectors.averagingInt(Integer::intValue)) == 0.0, "averagingInt empty");
        check(Stream.of(1, 9, 3).collect(Collectors.maxBy(Integer::compareTo)).get() == 9, "maxBy");
        check(Stream.of(1, 9, 3).collect(Collectors.minBy(Integer::compareTo)).get() == 1, "minBy");
        check(!Stream.<Integer>empty().collect(Collectors.maxBy(Integer::compareTo)).isPresent(), "maxBy empty");
        check(!Stream.<Integer>empty().collect(Collectors.minBy(Integer::compareTo)).isPresent(), "minBy empty");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.summingInt(null)), "summingInt(null) NPE");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.summingLong(null)), "summingLong(null) NPE");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.summingDouble(null)), "summingDouble(null) NPE");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.averagingInt(null)), "averagingInt(null) NPE");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.maxBy(null)), "maxBy(null) NPE");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.minBy(null)), "minBy(null) NPE");
    }

    static void testReducing() {
        check(Stream.of(1, 2, 3).collect(Collectors.reducing(0, Integer::intValue, Integer::sum)) == 6, "reducing 3-arg");
        check(Stream.<Integer>empty().collect(Collectors.reducing(10, Integer::intValue, Integer::sum)) == 10, "reducing 3-arg empty identity");
        check(Stream.of(1, 2, 3).collect(Collectors.reducing((a, b) -> a + b)).get() == 6, "reducing 1-arg");
        check(!Stream.<Integer>empty().collect(Collectors.reducing((a, b) -> a + b)).isPresent(), "reducing 1-arg empty");
        check(Stream.of(1, 2, 3).collect(Collectors.reducing(0, (a, b) -> a + b)) == 6, "reducing 2-arg");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.reducing(null, Integer::intValue, Integer::sum)), "reducing null identity NPE");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.reducing(0, null, Integer::sum)), "reducing null mapper NPE");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.reducing(0, Integer::intValue, null)), "reducing null op NPE");
    }

    static void testGroupingBy() {
        Map<Integer, List<Integer>> g = Stream.of(1, 2, 3, 4).collect(Collectors.groupingBy(x -> x % 2));
        check(g.get(0).equals(Arrays.asList(2, 4)) && g.get(1).equals(Arrays.asList(1, 3)), "groupingBy");
        Map<Integer, Long> gc = Stream.of("a", "bb", "cc", "ddd").collect(Collectors.groupingBy(String::length, Collectors.counting()));
        check(gc.get(1) == 1L && gc.get(2) == 2L && gc.get(3) == 1L, "groupingBy downstream counting");
        Map<Integer, Long> tree = Stream.of(2, 1).collect(Collectors.groupingBy(Function.identity(), java.util.TreeMap::new, Collectors.counting()));
        check(tree instanceof java.util.TreeMap, "groupingBy mapFactory type");
        check(Stream.<Integer>empty().collect(Collectors.groupingBy(Function.identity())).isEmpty(), "groupingBy empty");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.groupingBy(null)), "groupingBy(null) NPE");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.groupingBy(Function.identity(), null)), "groupingBy(fn,null) NPE");
    }

    static void testPartitioningBy() {
        Map<Boolean, List<Integer>> p = Stream.of(1, 2, 3).collect(Collectors.partitioningBy(x -> x > 1));
        check(p.get(true).equals(Arrays.asList(2, 3)) && p.get(false).equals(Arrays.asList(1)), "partitioningBy");
        Map<Boolean, Long> pc = Stream.of(1, 2, 3, 4).collect(Collectors.partitioningBy(x -> x % 2 == 0, Collectors.counting()));
        check(pc.get(true) == 2L && pc.get(false) == 2L, "partitioningBy downstream");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.partitioningBy(null)), "partitioningBy(null) NPE");
    }

    static void testToMap() {
        Map<Integer, Integer> m = Stream.of("a", "bb", "ccc").collect(Collectors.toMap(String::length, String::length));
        check(m.get(1) == 1 && m.get(2) == 2 && m.get(3) == 3, "toMap");
        Map<Integer, String> merged = Stream.of("a", "bb", "c", "dd").collect(
                Collectors.toMap(String::length, Function.identity(), (a, b) -> a + "+" + b));
        check(merged.get(1).equals("a+c") && merged.get(2).equals("bb+dd"), "toMap merge");
        checkThrows(IllegalStateException.class,
                () -> Stream.of("a", "bb", "c").collect(Collectors.toMap(String::length, Function.identity())),
                "toMap duplicate key ISE");
        checkThrows(NullPointerException.class, () -> Stream.of("a").collect(Collectors.toMap(null, Function.identity())), "toMap null keyMapper NPE");
        checkThrows(NullPointerException.class, () -> Stream.of("a").collect(Collectors.toMap(Function.identity(), null)), "toMap null valueMapper NPE");
        checkThrows(NullPointerException.class, () -> Stream.of("a").collect(Collectors.toMap(Function.identity(), Function.identity(), null)), "toMap null merge NPE");
        checkThrows(NullPointerException.class, () -> Stream.of("a").collect(Collectors.toMap(Function.identity(), Function.identity(), (a, b) -> a, null)), "toMap null supplier NPE");
    }

    static void testToConcurrentMap() {
        Map<Integer, Integer> cm = Stream.of("a", "bb", "ccc").collect(Collectors.toConcurrentMap(String::length, String::length));
        check(cm instanceof ConcurrentHashMap && cm.get(2) == 2, "toConcurrentMap");
        checkThrows(NullPointerException.class, () -> Stream.of("a").collect(Collectors.toConcurrentMap(null, Function.identity())), "toConcurrentMap null keyMapper NPE");
    }

    static void testSummarizing() {
        IntSummaryStatistics is = Stream.of(1, 2, 3).collect(Collectors.summarizingInt(Integer::intValue));
        check(is.getCount() == 3 && is.getSum() == 6 && is.getMin() == 1 && is.getMax() == 3, "summarizingInt");
        LongSummaryStatistics ls = Stream.of(1L, 2L).collect(Collectors.summarizingLong(Long::longValue));
        check(ls.getCount() == 2 && ls.getSum() == 3L, "summarizingLong");
        DoubleSummaryStatistics ds = Stream.of(1.0, 2.0).collect(Collectors.summarizingDouble(Double::doubleValue));
        check(ds.getCount() == 2 && ds.getSum() == 3.0, "summarizingDouble");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(Collectors.summarizingInt(null)), "summarizingInt(null) NPE");
    }

    public static void main(String[] args) {
        testToCollectionAndList();
        testJoining();
        testMappingCollectingAndThen();
        testCountingMinMaxSumming();
        testReducing();
        testGroupingBy();
        testPartitioningBy();
        testToMap();
        testToConcurrentMap();
        testSummarizing();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
