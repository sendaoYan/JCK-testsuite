/*
 * @test
 * @summary java.util.stream.Collectors conformance: white-box structure (final class / static
 *          member forms) + black-box behavior (toList/toSet/toMap, joining, groupingBy/
 *          partitioningBy, reductions, mapping/flatMapping, downstream/composite collectors).
 * @run main CollectorsTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.LongSummaryStatistics;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectorsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ===================== 白盒 / 结构（反射） =====================
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Collectors");
        check(!c.isInterface(), "Collectors is not an interface");
        check(Modifier.isPublic(c.getModifiers()), "Collectors is public");
        check(Modifier.isFinal(c.getModifiers()), "Collectors is final");
        check(c.getSuperclass() == Object.class, "Collectors extends Object");
        check(c.getInterfaces().length == 0, "Collectors implements no interfaces");

        check(hasMethod(c, "toList"), "Collectors#toList()");
        check(hasMethod(c, "toSet"), "Collectors#toSet()");
        check(hasMethod(c, "toUnmodifiableList"), "Collectors#toUnmodifiableList()");
        check(hasMethod(c, "toMap", Function.class, Function.class), "Collectors#toMap(k,v)");
        check(hasMethod(c, "toMap", Function.class, Function.class, BinaryOperator.class),
              "Collectors#toMap(k,v,merge)");
        check(hasMethod(c, "groupingBy", Function.class), "Collectors#groupingBy(classifier)");
        check(hasMethod(c, "partitioningBy", java.util.function.Predicate.class),
              "Collectors#partitioningBy(predicate)");
        check(hasMethod(c, "joining"), "Collectors#joining()");
        check(hasMethod(c, "counting"), "Collectors#counting()");
        check(hasMethod(c, "mapping", Function.class, java.util.stream.Collector.class),
              "Collectors#mapping(f, downstream)");
    }

    // ===================== 黑盒 / 行为 =====================
    static void testBasicCollectors() {
        List<Integer> list = Stream.of(1, 2, 3).collect(Collectors.toList());
        check(list.equals(List.of(1, 2, 3)), "toList ordered");

        Set<Integer> set = Stream.of(1, 1, 2).collect(Collectors.toSet());
        check(set.size() == 2 && set.contains(1) && set.contains(2), "toSet");

        List<Integer> col = Stream.of(1, 2).collect(Collectors.toCollection(ArrayList::new));
        check(col.equals(List.of(1, 2)), "toCollection");

        check("a-b-c".equals(Stream.of("a", "b", "c").collect(Collectors.joining("-"))),
              "joining delimiter");
        check("abc".equals(Stream.of("a", "b", "c").collect(Collectors.joining())),
              "joining no args");
        check("[a,b,c]".equals(Stream.of("a", "b", "c")
            .collect(Collectors.joining(",", "[", "]"))), "joining prefix/suffix");
    }

    static void testUnmodifiableCollectors() {
        List<Integer> ul = Stream.of(1, 2, 3).collect(Collectors.toUnmodifiableList());
        check(ul.equals(List.of(1, 2, 3)), "toUnmodifiableList contents");
        expectThrows(UnsupportedOperationException.class,
            () -> ul.add(4), "toUnmodifiableList immutable");

        Set<Integer> us = Stream.of(1, 2, 3).collect(Collectors.toUnmodifiableSet());
        check(us.size() == 3, "toUnmodifiableSet contents");
        expectThrows(UnsupportedOperationException.class,
            () -> us.add(4), "toUnmodifiableSet immutable");

        Map<Integer, String> um = Stream.of("a", "bb").collect(
            Collectors.toUnmodifiableMap(String::length, s -> s));
        check(um.get(1).equals("a") && um.get(2).equals("bb"), "toUnmodifiableMap contents");
        expectThrows(UnsupportedOperationException.class,
            () -> um.put(3, "x"), "toUnmodifiableMap immutable");
    }

    static void testToMap() {
        Map<Integer, Integer> toMap = Stream.of("a", "bb", "ccc").collect(
            Collectors.toMap(String::length, s -> s.length()));
        check(toMap.get(1) == 1 && toMap.get(3) == 3, "toMap");

        expectThrows(IllegalStateException.class,
            () -> Stream.of("a", "b", "cc").collect(Collectors.toMap(String::length, s -> s)),
            "toMap duplicate key");

        Map<Integer, String> merged = Stream.of("a", "b", "cc").collect(
            Collectors.toMap(String::length, s -> s, (s1, s2) -> s1 + s2));
        check(merged.get(1).equals("ab") && merged.get(2).equals("cc"), "toMap with merge");
    }

    static void testGroupingPartitioning() {
        Map<Boolean, List<Integer>> part = Stream.of(1, 2, 3).collect(
            Collectors.partitioningBy(x -> x > 1));
        check(part.get(true).equals(List.of(2, 3)) && part.get(false).equals(List.of(1)),
              "partitioningBy");

        Map<Boolean, Long> partCount = Stream.of(1, 2, 3, 4).collect(
            Collectors.partitioningBy(x -> x % 2 == 0, Collectors.counting()));
        check(partCount.get(true) == 2 && partCount.get(false) == 2,
              "partitioningBy with counting downstream");

        Map<Integer, List<Integer>> group = Stream.of(1, 2, 3).collect(
            Collectors.groupingBy(x -> x % 2));
        check(group.get(0).equals(List.of(2)) && group.get(1).equals(List.of(1, 3)),
              "groupingBy");

        Map<Integer, Long> groupCount = Stream.of("a", "bb", "cc", "d").collect(
            Collectors.groupingBy(String::length, Collectors.counting()));
        check(groupCount.get(1) == 2 && groupCount.get(2) == 2, "groupingBy counting downstream");

        Map<Integer, List<String>> groupTree = Stream.of("a", "bb", "cc", "ddd").collect(
            Collectors.groupingBy(String::length, TreeMap::new, Collectors.toList()));
        check(groupTree.keySet().toString().equals("[1, 2, 3]"), "groupingBy TreeMap key order");
    }

    static void testReductionCollectors() {
        check(Stream.of(1, 2, 3).collect(Collectors.counting()) == 3L, "counting");
        check(Stream.of(1, 2, 3).collect(Collectors.summingInt(Integer::intValue)) == 6, "summingInt");
        check(Stream.of(1L, 2L).collect(Collectors.summingLong(Long::longValue)) == 3L, "summingLong");
        check(Stream.of(1.0, 2.0).collect(Collectors.summingDouble(Double::doubleValue)) == 3.0,
              "summingDouble");
        check(Stream.of(1, 2, 3).collect(Collectors.averagingInt(Integer::intValue)) == 2.0,
              "averagingInt");
        check(Stream.<Integer>empty().collect(Collectors.averagingInt(Integer::intValue)) == 0.0,
              "averagingInt empty == 0.0");

        check(Stream.of(1, 9, 3).collect(Collectors.maxBy(Integer::compareTo)).get() == 9, "maxBy");
        check(Stream.of(1, 9, 3).collect(Collectors.minBy(Integer::compareTo)).get() == 1, "minBy");
        check(!Stream.<Integer>empty().collect(Collectors.minBy(Integer::compareTo)).isPresent(),
              "empty minBy");

        check(Stream.of(1, 2, 3).collect(Collectors.reducing(0, Integer::intValue, Integer::sum)) == 6,
              "reducing 3-arg");
        check(Stream.of(1, 2, 3).collect(Collectors.reducing(Integer::sum)).get() == 6,
              "reducing op");
        check(!Stream.<Integer>empty().collect(Collectors.reducing(Integer::sum)).isPresent(),
              "reducing op empty");
    }

    static void testMappingFlatMappingFiltering() {
        check("AB".equals(Stream.of("a", "b").collect(
            Collectors.mapping(String::toUpperCase, Collectors.joining()))), "mapping");

        List<Integer> fm = Stream.of(List.of(1, 2), List.of(3, 4)).collect(
            Collectors.flatMapping(List::stream, Collectors.toList()));
        check(fm.equals(List.of(1, 2, 3, 4)), "flatMapping");

        List<Integer> f = Stream.of(1, 2, 3, 4).collect(
            Collectors.filtering(x -> x % 2 == 0, Collectors.toList()));
        check(f.equals(List.of(2, 4)), "filtering");

        int size = Stream.of(1, 2, 3).collect(
            Collectors.collectingAndThen(Collectors.toList(), List::size));
        check(size == 3, "collectingAndThen");

        String tee = Stream.of(1, 2, 3, 4).collect(Collectors.teeing(
            Collectors.summingInt(Integer::intValue),
            Collectors.counting(),
            (sum, count) -> sum + "/" + count));
        check(tee.equals("10/4"), "teeing");
    }

    static void testSummarizing() {
        IntSummaryStatistics is = Stream.of(1, 2, 3).collect(
            Collectors.summarizingInt(Integer::intValue));
        check(is.getSum() == 6 && is.getCount() == 3 && is.getMin() == 1 && is.getMax() == 3,
              "summarizingInt");

        LongSummaryStatistics ls = Stream.of(1L, 2L, 3L).collect(
            Collectors.summarizingLong(Long::longValue));
        check(ls.getSum() == 6L && ls.getCount() == 3L, "summarizingLong");

        DoubleSummaryStatistics ds = Stream.of(1.0, 2.0, 3.0).collect(
            Collectors.summarizingDouble(Double::doubleValue));
        check(ds.getSum() == 6.0 && ds.getCount() == 3L, "summarizingDouble");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBasicCollectors();
        testUnmodifiableCollectors();
        testToMap();
        testGroupingPartitioning();
        testReductionCollectors();
        testMappingFlatMappingFiltering();
        testSummarizing();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
