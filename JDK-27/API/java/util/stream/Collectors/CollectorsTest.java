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

    static void testJava9Plus() {
        List<Integer> ul = Stream.of(1, 2, 3).collect(Collectors.toUnmodifiableList());
        check(ul.equals(Arrays.asList(1, 2, 3)), "toUnmodifiableList");
        checkThrows(UnsupportedOperationException.class, () -> ul.add(4), "toUnmodifiableList immutable");
        Set<Integer> us = Stream.of(1, 2).collect(Collectors.toUnmodifiableSet());
        check(us.size() == 2, "toUnmodifiableSet");
        checkThrows(UnsupportedOperationException.class, () -> us.add(3), "toUnmodifiableSet immutable");
        Map<Integer, String> um = Stream.of("a", "bb").collect(Collectors.toUnmodifiableMap(String::length, Function.identity()));
        check(um.get(1).equals("a") && um.get(2).equals("bb"), "toUnmodifiableMap");
        checkThrows(UnsupportedOperationException.class, () -> um.put(3, "x"), "toUnmodifiableMap immutable");
        String tee = Stream.of(1, 2, 3).collect(Collectors.teeing(
                Collectors.summingInt(Integer::intValue), Collectors.counting(),
                (sum, count) -> sum + ":" + count));
        check("6:3".equals(tee), "teeing");
        List<String> filtered = Stream.of(1, 2, 3, 4).collect(
                Collectors.filtering(x -> x % 2 == 0, Collectors.mapping(String::valueOf, Collectors.toList())));
        check(filtered.equals(Arrays.asList("2", "4")), "filtering");
        List<Integer> flat = Stream.of(Arrays.asList(1, 2), Arrays.asList(3)).collect(
                Collectors.flatMapping(java.util.Collection::stream, Collectors.toList()));
        check(flat.equals(Arrays.asList(1, 2, 3)), "flatMapping");
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


    // ===================== 契约深化：toMap 重复键/toSet/groupingBy 矩阵/collectingAndThen/joining 前后缀 =====================
    static void testContracts() {
        java.util.function.Function<String, Integer> len = String::length;
        // toSet 内容
        java.util.Set<Integer> lens = Stream.of("a", "bb", "cc", "ddd").map(String::length).collect(Collectors.toSet());
        check(lens.size() == 3 && lens.contains(1) && lens.contains(2) && lens.contains(3),
              "toSet deduplicates by equals");
        // toMap 重复键 -> IllegalStateException（消息两侧版本差异见断言）
        try {
            Stream.of("a", "b", "a").collect(Collectors.toMap(s -> s.length(), Function.identity()));
            check(false, "toMap duplicate key throws ISE");
        } catch (IllegalStateException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("Duplicate key 1"),
                      "toMap dup message (JDK-9+ form incl. values): " + e.getMessage());
        }
        // merge 函数覆盖分支
        java.util.Map<Integer, String> merged = Stream.of("ab", "cd", "ef")
              .collect(Collectors.toMap(k -> 1, Function.identity(), (a, b) -> a + "|" + b));
        check("ab|cd|ef".equals(merged.get(1)), "toMap merge folds in encounter order");
        // toMap null value -> NPE
        try {
            Stream.of("k").collect(Collectors.toMap(Function.identity(), s -> null));
            check(false, "toMap null value NPE");
        } catch (NullPointerException e) { check(true, "toMap null value NPE"); }
        try {
            Stream.of("k").collect(Collectors.toMap(null, Function.identity()));
            check(false, "toMap null keyMapper NPE");
        } catch (NullPointerException e) { check(true, "toMap null keyMapper NPE"); }
        // groupingBy：分类器返回 null -> NPE
        try {
            Stream.of("x").collect(Collectors.groupingBy(s -> (String) null));
            check(false, "groupingBy null classifier result NPE");
        } catch (NullPointerException e) { check(true, "groupingBy null classifier result NPE"); }
        // groupingBy 下游 counting / mapping
        java.util.Map<Integer, Long> counts = Stream.of("a", "bb", "cc", "ddd")
              .collect(Collectors.groupingBy(len, Collectors.counting()));
        check(counts.get(1) == 1L && counts.get(2) == 2L && counts.get(3) == 1L, "groupingBy + counting");
        java.util.Map<Integer, java.util.List<Integer>> byLenUpper = Stream.of("ab", "cd", "e")
              .collect(Collectors.groupingBy(len, Collectors.mapping(String::hashCode, Collectors.toList())));
        check(byLenUpper.get(1).size() == 1 && byLenUpper.get(2).size() == 2, "groupingBy + mapping downstream");
        // groupingBy(mapFactory) TreeMap -> 键有序
        java.util.Map<Integer, Long> tree = Stream.of("ddd", "a", "bb")
              .collect(Collectors.groupingBy(len, java.util.TreeMap::new, Collectors.counting()));
        check(tree instanceof java.util.TreeMap && new java.util.ArrayList<Object>(tree.keySet())
              .equals(Arrays.asList(1, 2, 3)), "TreeMap factory keeps keys sorted");
        // partitioningBy
        java.util.Map<Boolean, java.util.List<Integer>> parts = Stream.of(1, 2, 3, 4)
              .collect(Collectors.partitioningBy(i -> i % 2 == 0));
        check(parts.get(true).equals(Arrays.asList(2, 4)) && parts.get(false).equals(Arrays.asList(1, 3)),
              "partitioningBy both halves");
        // collectingAndThen：不可变结果
        java.util.List<Integer> ro = Stream.of(1, 2, 3)
              .collect(Collectors.collectingAndThen(Collectors.toList(), java.util.Collections::unmodifiableList));
        try {
            ro.add(4);
            check(false, "collectingAndThen unmodifiable add UOE");
        } catch (UnsupportedOperationException e) { check(true, "collectingAndThen unmodifiable add UOE"); }
        // joining 前缀/分隔/后缀
        check("[a,b,c]".equals(Stream.of("a", "b", "c")
              .collect(Collectors.joining(",", "[", "]"))), "joining with delimiters");
        check("[]".equals(Stream.<String>empty().collect(Collectors.joining(",", "[", "]"))),
              "empty joining is prefix+suffix only");
        // averaging -> OptionalDouble
        check(Stream.of(1, 2, 3, 4).collect(Collectors.averagingInt(Integer::intValue)) == 2.5,
              "averagingInt exact (returns Double)");
        check(Stream.<Integer>empty().collect(Collectors.averagingInt(Integer::intValue)) == 0.0,
              "empty averaging returns 0.0 (locked)");
        // counting 空流 0L、summarizing 全字段
        check(Stream.of().collect(Collectors.counting()) == 0L, "counting empty 0");
        java.util.IntSummaryStatistics st = Stream.of("aa", "b", "ccc")
              .collect(Collectors.summarizingInt(String::length));
        check(st.getCount() == 3 && st.getSum() == 6 && st.getMin() == 1 && st.getMax() == 3 && st.getAverage() == 2.0,
              "summarizingInt all fields");
        // toCollection LinkedHashSet 保插入序
        java.util.Set<String> ordered = Stream.of("z", "a", "m")
              .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        check(new java.util.ArrayList<String>(ordered).equals(Arrays.asList("z", "a", "m")),
              "toCollection preserves encounter order");
        // reducing 两参
        check(Stream.of(1, 2, 3).collect(Collectors.reducing(0, Integer::intValue, Integer::sum)) == 6,
              "reducing 3-arg total");
        // toMap 空 -> 空 map；parallel collect 一致
        check(Stream.<String>empty().collect(Collectors.toMap(Function.identity(), Function.identity())).isEmpty(),
              "toMap empty");
        java.util.Map<Integer, String> par = java.util.stream.IntStream.range(0, 50).parallel().boxed()
              .collect(Collectors.toMap(i -> i, String::valueOf));
        check(par.size() == 50 && "49".equals(par.get(49)), "parallel toMap complete");
    }


    // ===================== JDK-10+/12+ 增量：toUnmodifiable*/flatMapping/filtering/teeing（JDK-27 侧锁定） =====================
    static void testJdk27Additions() {
        java.util.List<String> ul = Stream.of("a", "b").collect(Collectors.toUnmodifiableList());
        try { ul.add("c"); check(false, "toUnmodifiableList UOE"); }
        catch (UnsupportedOperationException e) { check(true, "toUnmodifiableList UOE"); }
        check(ul.equals(Arrays.asList("a", "b")), "toUnmodifiableList contents");
        java.util.Set<Integer> us = Stream.of(1, 2, 2, 3).collect(Collectors.toUnmodifiableSet());
        try { us.add(4); check(false, "toUnmodifiableSet UOE"); }
        catch (UnsupportedOperationException e) { check(true, "toUnmodifiableSet UOE"); }
        check(us.size() == 3, "toUnmodifiableSet dedup");
        java.util.Map<Integer, String> um = Stream.of(1, 2)
              .collect(Collectors.toUnmodifiableMap(i -> i, String::valueOf));
        try { um.put(3, "x"); check(false, "toUnmodifiableMap put UOE"); }
        catch (UnsupportedOperationException e) { check(true, "toUnmodifiableMap put UOE"); }
        try {
            Stream.of("ab", "cd").collect(Collectors.toUnmodifiableMap(k -> 1, Function.identity()));
            check(false, "toUnmodifiableMap dup ISE");
        } catch (IllegalStateException e) { check(true, "toUnmodifiableMap dup ISE"); }
        // flatMapping 下游
        java.util.Map<Boolean, java.util.List<Integer>> fm = Stream.of("ab", "c", "de")
              .collect(Collectors.groupingBy(s -> s.length() == 1,
                      Collectors.flatMapping(s -> s.chars().boxed(), Collectors.toList())));
        check(fm.get(true).size() == 1 && fm.get(false).size() == 4, "flatMapping flattens downstream");
        // filtering 下游
        java.util.Map<Integer, Long> fl = Stream.of("a", "bb", "cc", "ddd")
              .collect(Collectors.groupingBy(String::length, Collectors.filtering(s -> !s.startsWith("b"), Collectors.counting())));
        check(fl.get(2) == 1L, "filtering downstream");
        // teeing
        java.util.function.BiFunction<Long, Long, Double> avg =
              (sum, cnt) -> cnt == 0 ? 0.0 : sum.doubleValue() / cnt;
        Double mean = Stream.of(1, 2, 3, 4).collect(Collectors.teeing(
                Collectors.summingLong(Integer::longValue),
                Collectors.counting(),
                avg));
        check(mean == 2.5, "teeing computes mean");
        String joined = Stream.of("x", "y").collect(Collectors.teeing(
                Collectors.joining(","), Collectors.joining("|"), (a, b) -> a + "/" + b));
        check("x,y/x|y".equals(joined), "teeing both results");
    }

    public static void main(String[] args) {
        testJdk27Additions();
        testContracts();
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
        testJava9Plus();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
