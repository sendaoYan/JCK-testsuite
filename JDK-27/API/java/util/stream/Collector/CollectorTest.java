/*
 * @test
 * @summary java.util.stream.Collector contract: of() factory forms, supplier/accumulator/
 *          combiner/finisher, characteristics, null->NPE for each argument.
 * @run main CollectorTest
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class CollectorTest {
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

    static Supplier<List<String>> supplier() { return ArrayList::new; }
    static BiConsumer<List<String>, String> accumulator() { return List::add; }
    static BinaryOperator<List<String>> combiner() { return (a, b) -> { a.addAll(b); return a; }; }

    static void testCollectorOf() {
        Collector<String, ?, List<String>> c = Collector.of(supplier(), accumulator(), combiner());
        List<String> out = Stream.of("a", "b").collect(c);
        check(out.size() == 2 && out.contains("a"), "collect");
        check(c.supplier().get() instanceof List, "supplier");
        check(c.accumulator() != null && c.combiner() != null, "accumulator/combiner");
        check(c.finisher() != null, "identity finisher");
        check(c.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH), "identity-finish char");

        Function<List<String>, Set<String>> finisher = s -> new java.util.HashSet<>(s);
        Collector<String, ?, Set<String>> c2 = Collector.of(supplier(), accumulator(), combiner(), finisher,
                Collector.Characteristics.UNORDERED, Collector.Characteristics.CONCURRENT);
        Set<String> set = Stream.of("a", "b", "a").collect(c2);
        check(set.size() == 2, "collect with finisher");
        check(c2.finisher() == finisher, "explicit finisher");
        check(c2.characteristics().contains(Collector.Characteristics.UNORDERED), "UNORDERED char");
        check(c2.characteristics().contains(Collector.Characteristics.CONCURRENT), "CONCURRENT char");
        check(c2.characteristics().size() == 2, "characteristics size");
    }

    static void testNullArguments() {
        checkThrows(NullPointerException.class, () -> Collector.of(null, accumulator(), combiner()), "of null supplier NPE");
        checkThrows(NullPointerException.class, () -> Collector.of(supplier(), null, combiner()), "of null accumulator NPE");
        checkThrows(NullPointerException.class, () -> Collector.of(supplier(), accumulator(), null), "of null combiner NPE");
        checkThrows(NullPointerException.class,
                () -> Collector.of(supplier(), accumulator(), combiner(),
                        (Function<List<String>, Set<String>>) null, Collector.Characteristics.UNORDERED),
                "of null finisher NPE");
    }

    static void testCharacteristicsEnum() {
        for (Collector.Characteristics ch : Collector.Characteristics.values()) {
            check(ch != null, "characteristic " + ch);
        }
        check(Collector.Characteristics.CONCURRENT != Collector.Characteristics.UNORDERED, "distinct enum values");
        check(Collector.Characteristics.valueOf("IDENTITY_FINISH") == Collector.Characteristics.IDENTITY_FINISH, "valueOf");
    }


    // ===================== 契约深化：自定义收集器调用记录/IDENTITY_FINISH 不调 finisher/并行 combiner =====================
    static void testContracts() {
        final java.util.List<String> calls = java.util.Collections.synchronizedList(new java.util.ArrayList<String>());
        Collector<Integer, java.util.List<Integer>, java.util.List<Integer>> recording = Collector.of(
                () -> (java.util.List<Integer>) java.util.Collections.synchronizedList(new java.util.ArrayList<Integer>()),
                (list, i) -> { list.add(i); calls.add("acc"); },
                (l, r) -> { l.addAll(r); calls.add("comb"); return l; },
                java.util.stream.Collector.Characteristics.IDENTITY_FINISH);
        // 顺序：acc n 次、无 finisher
        java.util.List<Integer> seq = Stream.of(1, 2, 3).collect(recording);
        check(seq.equals(java.util.Arrays.asList(1, 2, 3)) && calls.stream().filter(c -> c.equals("acc")).count() == 3,
              "sequential accumulates per element");
        // 并行：combiner 被调用，结果完整
        calls.clear();
        Integer[] big = new Integer[10000];
        for (int i = 0; i < big.length; i++) big[i] = i;
        java.util.List<Integer> par = java.util.Arrays.stream(big).parallel().collect(recording);
        check(par.size() == 10000, "parallel combiner merges complete result");
        check(calls.contains("comb"), "combiner invoked on parallel collect");
        // 非 IDENTITY_FINISH：finisher 应用
        Collector<Integer, ?, Integer> counting = Collector.of(
                () -> (java.util.List<Integer>) java.util.Collections.synchronizedList(new java.util.ArrayList<Integer>()),
                java.util.List::add,
                (l, r) -> { l.addAll(r); return l; },
                java.util.List::size);
        check(Stream.of(1, 2, 3, 4).collect(counting) == 4, "finisher transforms result");
        // characteristics 透传
        Collector<String, ?, java.util.List<String>> withChars = Collector.of(
                () -> (java.util.List<String>) java.util.Collections.synchronizedList(new java.util.ArrayList<String>()), java.util.List::add,
                (l, r) -> { l.addAll(r); return l; },
                java.util.stream.Collector.Characteristics.CONCURRENT,
                java.util.stream.Collector.Characteristics.UNORDERED);
        check(withChars.characteristics().contains(java.util.stream.Collector.Characteristics.CONCURRENT)
              && withChars.characteristics().contains(java.util.stream.Collector.Characteristics.UNORDERED),
              "characteristics propagated");
        check(withChars.characteristics().contains(java.util.stream.Collector.Characteristics.IDENTITY_FINISH),
              "4-arg of() implies identity finisher -> IDENTITY_FINISH present");
        // 5 参 of()（非恒等 finisher）不含 IDENTITY_FINISH
        Collector<Integer, ?, Integer> sized = Collector.of(
                () -> (java.util.List<Integer>) java.util.Collections.synchronizedList(new java.util.ArrayList<Integer>()),
                java.util.List::add,
                (l, r) -> { l.addAll(r); return l; },
                java.util.List::size,
                java.util.stream.Collector.Characteristics.CONCURRENT,
                java.util.stream.Collector.Characteristics.UNORDERED);
        check(!sized.characteristics().contains(java.util.stream.Collector.Characteristics.IDENTITY_FINISH),
              "non-identity finisher -> no IDENTITY_FINISH");
        // Characteristics 枚举面
        check(java.util.stream.Collector.Characteristics.values().length == 3, "exactly 3 characteristics");
    }

    public static void main(String[] args) {
        testContracts();
        testCollectorOf();
        testNullArguments();
        testCharacteristicsEnum();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
