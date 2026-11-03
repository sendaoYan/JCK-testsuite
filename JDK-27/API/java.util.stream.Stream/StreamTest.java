/*
 * @test
 * @summary java.util.stream.Stream conformance: white-box structure (interface/BaseStream/
 *          member forms) + black-box behavior (factories, lazy pipeline, short-circuit,
 *          terminal reductions).
 * @run main StreamTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.BaseStream;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamTest {
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
        Class<?> c = Class.forName("java.util.stream.Stream");
        check(c.isInterface(), "Stream is an interface");
        check(Modifier.isPublic(c.getModifiers()), "Stream is public");
        check(c.getSuperclass() == null, "interface superclass is null");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0] == BaseStream.class,
              "Stream extends BaseStream");
        check(BaseStream.class.isInterface()
              && BaseStream.class.getInterfaces()[0] == AutoCloseable.class,
              "BaseStream is an interface extending AutoCloseable");

        // 关键成员形态
        check(hasMethod(c, "filter", Predicate.class), "Stream#filter(Predicate)");
        check(hasMethod(c, "map", Function.class), "Stream#map(Function)");
        check(hasMethod(c, "flatMap", Function.class), "Stream#flatMap(Function)");
        check(hasMethod(c, "reduce", BinaryOperator.class), "Stream#reduce(BinaryOperator)");
        check(hasMethod(c, "reduce", Object.class, BinaryOperator.class), "Stream#reduce(identity, op)");
        check(hasMethod(c, "collect", Collector.class), "Stream#collect(Collector)");
        check(hasMethod(c, "count"), "Stream#count()");
        check(hasMethod(c, "toList"), "Stream#toList()");
        check(hasMethod(c, "of", Object.class), "Stream#of(T)");
        check(hasMethod(c, "ofNullable", Object.class), "Stream#ofNullable(T)");
        check(hasMethod(c, "empty"), "Stream#empty()");
        check(hasMethod(c, "iterate", Object.class, UnaryOperator.class), "Stream#iterate(seed, unary)");
    }

    // ===================== 黑盒 / 行为 =====================
    static void testFactories() {
        check(Stream.of(1, 2, 3).count() == 3, "of varargs");
        check(Stream.of(7).count() == 1, "of single");
        check(Stream.empty().count() == 0, "empty");
        check(Stream.ofNullable(null).count() == 0, "ofNullable(null) empty");
        check(Stream.ofNullable("x").count() == 1, "ofNullable(value) single");
        check(Stream.concat(Stream.of(1, 2), Stream.of(3)).count() == 3, "concat");

        check(Stream.generate(() -> "g").limit(3).count() == 3, "generate+limit");
        List<Integer> it1 = Stream.iterate(1, x -> x + 1).limit(4).collect(Collectors.toList());
        check(it1.equals(List.of(1, 2, 3, 4)), "iterate 2-arg");
        List<Integer> it3 = Stream.iterate(1, x -> x <= 4, x -> x + 1).collect(Collectors.toList());
        check(it3.equals(List.of(1, 2, 3, 4)), "iterate 3-arg (hasNext predicate)");

        Stream.Builder<Integer> b = Stream.builder();
        b.add(1).add(2).add(3);
        check(b.build().mapToInt(Integer::intValue).sum() == 6, "builder");

        // 单参 of(T) 不检查 null：产生含单个 null 的流（区别于 ofNullable）
        check(Stream.of((String) null).count() == 1, "of(null) yields one null element");
        check(Stream.of((String) null).filter(x -> x != null).count() == 0,
              "of(null) element can be filtered out");
    }

    static void testLazinessAndShortCircuit() {
        final int[] side = {0};
        Stream<Integer> lazy = Stream.of(1, 2, 3, 4).map(x -> { side[0]++; return x * 10; });
        check(side[0] == 0, "map not executed before terminal op");
        List<Integer> out = lazy.collect(Collectors.toList());
        check(out.equals(List.of(10, 20, 30, 40)), "map applied lazily on terminal");
        check(side[0] == 4, "map executed exactly once per element");

        final int[] seen = {0};
        List<Integer> limited = Stream.iterate(0, x -> x + 1)
            .map(x -> { seen[0]++; return x; })
            .limit(2)
            .collect(Collectors.toList());
        check(limited.equals(List.of(0, 1)), "limit terminates infinite stream");
        check(seen[0] == 2, "limit short-circuits (only 2 mapped)");

        check(Stream.iterate(1, x -> x + 1).filter(x -> x > 5).findFirst().get() == 6,
              "findFirst short-circuits infinite stream");

        final int[] peeked = {0};
        List<Integer> peekedList = Stream.of(1, 2, 3).peek(x -> peeked[0]++)
            .collect(Collectors.toList());
        check(peekedList.equals(List.of(1, 2, 3)) && peeked[0] == 3,
              "peek invoked once per element on full traversal");

        List<Integer> afterSkip = Stream.of(1, 2, 3, 4, 5).skip(3).collect(Collectors.toList());
        check(afterSkip.equals(List.of(4, 5)), "skip reduces downstream elements");
    }

    static void testIntermediateOps() {
        List<Integer> pipe = Stream.of(5, 1, 3, 1, 2)
            .filter(x -> x > 0)
            .map(x -> x * 10)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        check(pipe.equals(List.of(10, 20, 30, 50)), "filter/map/distinct/sorted chain");

        List<Integer> tw = Stream.of(1, 2, 3, 4).takeWhile(x -> x < 3).collect(Collectors.toList());
        check(tw.equals(List.of(1, 2)), "takeWhile");
        List<Integer> dw = Stream.of(1, 2, 3, 4).dropWhile(x -> x < 3).collect(Collectors.toList());
        check(dw.equals(List.of(3, 4)), "dropWhile");

        List<Integer> mm = Stream.of(1, 2, 3)
            .<Integer>mapMulti((x, sink) -> { sink.accept(x); sink.accept(x * 10); })
            .collect(Collectors.toList());
        check(mm.equals(List.of(1, 10, 2, 20, 3, 30)), "mapMulti");

        check(Stream.of(1, 2, 3).flatMap(x -> Stream.of(x, x)).count() == 6, "flatMap");
        check(Stream.of("a", "b", "c").sorted((x, y) -> y.compareTo(x)).findFirst().get().equals("c"),
              "sorted with comparator");
        check(Stream.of(1, 2, 3).skip(5).count() == 0, "skip beyond end");
        check(Stream.of(1, 2, 3).limit(0).count() == 0, "limit zero");
    }

    static void testTerminalOps() {
        check(Stream.of(1, 2, 3, 4).reduce(0, Integer::sum) == 10, "reduce identity");
        check(Stream.of(1, 2, 3, 4).reduce(Integer::sum).get() == 10, "reduce op");
        check(!Stream.<Integer>empty().reduce(Integer::sum).isPresent(), "reduce op empty -> empty Optional");
        check(Stream.of(1, 2, 3, 4).reduce(0, Integer::sum, Integer::sum) == 10, "reduce 3-arg");

        check(Stream.of(1, 2, 3, 4).count() == 4, "count");
        check(Stream.of(1, 2, 3).min(Integer::compareTo).get() == 1, "min");
        check(Stream.of(1, 2, 3).max(Integer::compareTo).get() == 3, "max");
        check(!Stream.<Integer>empty().min(Integer::compareTo).isPresent(), "empty min");
        check(!Stream.<Integer>empty().max(Integer::compareTo).isPresent(), "empty max");
        check(!Stream.<Integer>empty().findFirst().isPresent(), "empty findFirst");
        check(!Stream.<Integer>empty().findAny().isPresent(), "empty findAny");
        check(Stream.of("a", "b").findFirst().get().equals("a"), "findFirst encounter order");
        check(Stream.of("a", "b").findAny().isPresent(), "findAny present");

        check(Stream.of(1, 2, 3).allMatch(x -> x > 0), "allMatch");
        check(Stream.of(1, 2, 3).anyMatch(x -> x == 2), "anyMatch");
        check(Stream.of(1, 2, 3).noneMatch(x -> x > 5), "noneMatch");
        check(Stream.<Integer>empty().allMatch(x -> false), "allMatch empty is true");
        check(Stream.<Integer>empty().noneMatch(x -> true), "noneMatch empty is true");

        List<Integer> ordered = new ArrayList<>();
        Stream.of(3, 1, 2).forEachOrdered(ordered::add);
        check(ordered.equals(List.of(3, 1, 2)), "forEachOrdered encounter order");

        Object[] objs = Stream.of("x", "y").toArray();
        check(objs.length == 2 && objs[0].equals("x"), "toArray Object[]");
        String[] strs = Stream.of("x", "y").toArray(String[]::new);
        check(strs.length == 2 && strs[1].equals("y"), "toArray IntFunction");

        List<Integer> immutable = Stream.of(1, 2, 3).toList();
        check(immutable.equals(List.of(1, 2, 3)), "toList contents");
        expectThrows(UnsupportedOperationException.class,
            () -> immutable.add(4), "toList is immutable");

        List<Integer> collected3 = Stream.of(1, 2, 3)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        check(collected3.equals(List.of(1, 2, 3)), "collect 3-arg");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testFactories();
        testLazinessAndShortCircuit();
        testIntermediateOps();
        testTerminalOps();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
