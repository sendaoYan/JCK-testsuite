/*
 * @test
 * @summary java.util.stream.Stream contract: factories, lazy intermediate ops,
 *          terminal ops, reduce/collect/min/max/match, stream single-use, parallel
 *          consistency, null->NPE / bad-arg->IAE, toArray contract, onClose order,
 *          and empty-stream semantics.
 * @run main StreamTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamTest {
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

    static void testFactories() {
        check(Stream.empty().count() == 0, "empty");
        check(Stream.of(1).count() == 1, "of single");
        check(Stream.of(1, 2, 3).count() == 3, "of varargs");
        check(Stream.of(new Integer[]{1, 2}).count() == 2, "of array varargs");
        check(Stream.concat(Stream.of(1), Stream.of(2, 3)).count() == 3, "concat");
        check(Stream.generate(() -> 1).limit(5).count() == 5, "generate");
        List<Integer> it = Stream.iterate(1, x -> x + 1).limit(4).collect(Collectors.toList());
        check(it.equals(Arrays.asList(1, 2, 3, 4)), "iterate");
        Stream.Builder<Integer> b = Stream.builder();
        b.add(1).add(2).add(3);
        check(b.build().mapToInt(Integer::intValue).sum() == 6, "builder");
    }

    static void testLaziness() {
        final int[] calls = new int[1];
        Stream<Integer> s = Stream.of(1, 2, 3).map(x -> { calls[0]++; return x; })
                                   .filter(x -> x > 0);
        check(calls[0] == 0, "intermediate ops are lazy before terminal");
        s.count();
        check(calls[0] == 3, "map invoked once per element after terminal");
    }

    static void testIntermediateOps() {
        check(Stream.of(1, 2, 3).map(x -> x * 10).collect(Collectors.toList())
                .equals(Arrays.asList(10, 20, 30)), "map");
        check(Stream.of(1, 2, 3).filter(x -> x % 2 == 1).collect(Collectors.toList())
                .equals(Arrays.asList(1, 3)), "filter");
        check(Stream.of(1, 2, 3).flatMap(x -> Stream.of(x, x)).count() == 6, "flatMap");
        check(Stream.of(3, 1, 2).sorted().collect(Collectors.toList())
                .equals(Arrays.asList(1, 2, 3)), "sorted natural");
        check(Stream.of(3, 1, 2).sorted(Comparator.reverseOrder()).collect(Collectors.toList())
                .equals(Arrays.asList(3, 2, 1)), "sorted comparator");
        check(Stream.of(1, 1, 2, 2, 3).distinct().collect(Collectors.toList())
                .equals(Arrays.asList(1, 2, 3)), "distinct");
        final int[] seen = new int[1];
        check(Stream.of(1, 2, 3).peek(x -> seen[0]++).collect(Collectors.toList())
                .equals(Arrays.asList(1, 2, 3)), "peek passthrough");
        check(seen[0] == 3, "peek invoked per element");
        check(Stream.of(1, 2, 3, 4).limit(2).collect(Collectors.toList())
                .equals(Arrays.asList(1, 2)), "limit");
        check(Stream.of(1, 2, 3, 4).skip(2).collect(Collectors.toList())
                .equals(Arrays.asList(3, 4)), "skip");
        check(Stream.of(1, 2, 3).limit(0).count() == 0, "limit 0");
        check(Stream.of(1, 2, 3).skip(10).count() == 0, "skip beyond");
        check(Stream.of(1, 2, 3).mapToInt(Integer::intValue).sum() == 6, "mapToInt");
        check(Stream.of(1L, 2L).mapToLong(Long::longValue).sum() == 3L, "mapToLong");
        check(Stream.of(1.0, 2.0).mapToDouble(Double::doubleValue).sum() == 3.0, "mapToDouble");
        check(Stream.of(1, 2).flatMapToInt(x -> java.util.stream.IntStream.of(x)).sum() == 3, "flatMapToInt");
    }

    static void testTerminalReduce() {
        check(Stream.of(1, 2, 3, 4).reduce(0, (a, b) -> a + b) == 10, "reduce identity");
        check(Stream.of(1, 2, 3).reduce((a, b) -> a + b).get() == 6, "reduce no identity");
        check(!Stream.<Integer>empty().reduce((a, b) -> a + b).isPresent(), "empty reduce -> empty");
        check(Stream.of("a", "b", "c").reduce("", (a, b) -> a + b).equals("abc"), "reduce concat");
        check(Stream.of(1, 2, 3).reduce(10, (a, b) -> a + b) == 16, "reduce identity non-zero");
    }

    static void testTerminalCollect() {
        check(Stream.of(1, 2, 3).collect(Collectors.toList()).equals(Arrays.asList(1, 2, 3)), "collect toList");
        check(Stream.of("a", "b").collect(Collectors.joining("-")).equals("a-b"), "collect joining");
        ArrayList<Integer> target = Stream.of(1, 2, 3).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        check(target.equals(Arrays.asList(1, 2, 3)), "collect 3-arg");
        check(Stream.of(1, 2, 3).count() == 3, "count");
    }

    static void testTerminalMatchMinMax() {
        check(Stream.of(1, 2, 3).anyMatch(x -> x == 2), "anyMatch");
        check(!Stream.of(1, 2, 3).anyMatch(x -> x == 9), "anyMatch false");
        check(Stream.of(1, 2, 3).allMatch(x -> x > 0), "allMatch");
        check(!Stream.of(1, 2, 3).allMatch(x -> x > 1), "allMatch false");
        check(Stream.of(1, 2, 3).noneMatch(x -> x > 5), "noneMatch");
        check(!Stream.of(1, 2, 3).noneMatch(x -> x == 2), "noneMatch false");
        check(Stream.of(5, 1, 9).min(Integer::compareTo).get() == 1, "min");
        check(Stream.of(5, 1, 9).max(Integer::compareTo).get() == 9, "max");
        check(!Stream.<Integer>empty().min(Integer::compareTo).isPresent(), "empty min");
        check(Stream.of(3, 2, 1).findFirst().get() == 3, "findFirst order");
        check(!Stream.<Integer>empty().findFirst().isPresent(), "empty findFirst");
        check(Stream.of(1, 2, 3).findAny().isPresent(), "findAny present");
    }

    static void testToArray() {
        Object[] oa = Stream.of(1, 2, 3).toArray();
        check(oa.length == 3 && ((Integer) oa[0]) == 1, "toArray Object[]");
        Integer[] ia = Stream.of(1, 2, 3).toArray(Integer[]::new);
        check(ia.length == 3 && ia[2] == 3, "toArray generator sized");
        Integer[] empty = Stream.<Integer>empty().toArray(Integer[]::new);
        check(empty.length == 0, "toArray empty");
        Integer[] big = new Integer[10];
        Integer[] reused = Stream.of(1, 2).filter(x -> true).toArray(n -> big);
        check(reused == big, "toArray reuses provided array for unknown-size stream");
        check(big[0] == 1 && big[1] == 2 && big[2] == null, "toArray fills then null terminates");
    }

    static void testSingleUse() {
        Stream<Integer> s = Stream.of(1, 2, 3);
        check(s.count() == 3, "first consumption");
        checkThrows(IllegalStateException.class, () -> s.count(), "stream consumed twice -> ISE");
        Stream<Integer> s2 = Stream.of(1, 2, 3);
        java.util.Iterator<Integer> it = s2.iterator();
        check(it.hasNext() && it.next() == 1, "iterator first");
        checkThrows(IllegalStateException.class, () -> s2.count(), "stream has iterator -> ISE on terminal");
    }

    static void testParallelConsistency() {
        int serial = Stream.of(1, 2, 3, 4, 5, 6, 7, 8).mapToInt(Integer::intValue).sum();
        int parallel = Stream.of(1, 2, 3, 4, 5, 6, 7, 8).parallel().mapToInt(Integer::intValue).sum();
        check(serial == parallel, "parallel sum equals serial");
        check(Stream.of(1, 2, 3).parallel().isParallel(), "isParallel true");
        check(!Stream.of(1, 2, 3).sequential().isParallel(), "isParallel false");
        List<Integer> sorted = Stream.of(3, 1, 2).parallel().sorted().collect(Collectors.toList());
        check(sorted.equals(Arrays.asList(1, 2, 3)), "parallel sorted deterministic");
    }

    static void testNullAndBadArgs() {
        check(Stream.of((Object) null).count() == 1, "of(null) allows single null element");
        check(Stream.of(1, (Integer) null).count() == 2, "of(...,null) allows null element");
        checkThrows(NullPointerException.class, () -> Stream.of(1).filter(null), "filter(null)");
        checkThrows(NullPointerException.class, () -> Stream.of(1).map(null), "map(null)");
        checkThrows(NullPointerException.class, () -> Stream.of(1).flatMap(null), "flatMap(null)");
        checkThrows(NullPointerException.class, () -> Stream.of(1).sorted((Comparator) null), "sorted(null)");
        checkThrows(IllegalArgumentException.class, () -> Stream.of(1).limit(-1), "limit(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> Stream.of(1).skip(-1), "skip(-1) IAE");
        checkThrows(NullPointerException.class, () -> Stream.of(1).reduce((java.util.function.BinaryOperator<Integer>) null), "reduce null accumulator NPE");
        checkThrows(NullPointerException.class, () -> Stream.of(1).collect(null, (a, b) -> {}, (a, b) -> {}), "collect null supplier NPE");
    }

    static void testOnClose() {
        final int[] order = new int[2];
        final int[] step = new int[1];
        try (Stream<Integer> s = Stream.of(1, 2).onClose(() -> { order[0] = step[0]++; })
                                                .onClose(() -> { order[1] = step[0]++; })) {
            check(step[0] == 0, "onClose not run until close");
        }
        check(step[0] == 2, "both close handlers run");
        check(order[0] == 0 && order[1] == 1, "close handlers run in reverse registration order");
    }


    // ===================== 契约深化：并行 encounter order/短路 peek 计数/稳定排序/concat/suppressed close =====================
    static void testContracts() {
        // forEachOrdered：并行流保证 encounter order
        final java.util.List<Integer> order = java.util.Collections.synchronizedList(new java.util.ArrayList<Integer>());
        java.util.stream.IntStream.range(0, 100).parallel().boxed().forEachOrdered(order::add);
        check(order.size() == 100 && order.get(0) == 0 && order.get(99) == 99,
              "parallel forEachOrdered keeps encounter order");
        // findFirst：并行流仍返回 encounter order 第一个
        check(java.util.stream.IntStream.range(0, 100).parallel().boxed().findFirst().get() == 0,
              "parallel findFirst returns first in encounter order");
        // 短路：anyMatch 停止后上游不再处理（peek 计数）
        final int[] seen = {0};
        boolean any = Stream.iterate(0, i -> i + 1).peek(i -> seen[0]++)
                .anyMatch(i -> i == 3);
        check(any && seen[0] <= 4, "anyMatch short-circuits upstream (saw " + seen[0] + ")");
        // limit 影响上游处理次数（无限流 + limit 有限处理）
        final int[] mapped = {0};
        long cnt = Stream.iterate(1, i -> i + 1).map(i -> { mapped[0]++; return i; }).limit(5).count();
        check(cnt == 5 && mapped[0] == 5, "limit bounds upstream mapping exactly (saw " + mapped[0] + ")");
        // skip(n) 后余量
        check(Stream.of(1, 2, 3, 4, 5).skip(2).collect(Collectors.toList()).equals(Arrays.asList(3, 4, 5)),
              "skip leaves tail");
        check(Stream.of(1, 2).skip(5).count() == 0, "skip past end empty");
        // sorted 稳定性：同键保持插入序
        java.util.List<String> stable = Stream.of("b2", "a1", "b1", "a2", "c1")
                .sorted(java.util.Comparator.comparing((String s) -> s.substring(0, 1)))
                .collect(Collectors.toList());
        check(stable.equals(Arrays.asList("a1", "a2", "b2", "b1", "c1")), "sorted is stable: " + stable);
        // flatMap 一对多/空
        check(Stream.of(1, 2, 3).flatMap(i -> i == 2 ? Stream.<Integer>empty() : Stream.of(i, i * 10))
              .collect(Collectors.toList()).equals(Arrays.asList(1, 10, 3, 30)), "flatMap flattens incl. empty");
        // concat 保序 + 元素计数
        check(Stream.concat(Stream.of(1, 2), Stream.of(3)).collect(Collectors.toList())
              .equals(Arrays.asList(1, 2, 3)), "concat keeps order");
        check(Stream.concat(Stream.empty(), Stream.empty()).count() == 0, "concat of empties empty");
        // max/min 空流 Optional.empty
        check(!Stream.<Integer>empty().max(Integer::compare).isPresent(), "empty max Optional.empty");
        check(!Stream.<Integer>empty().min(Integer::compare).isPresent(), "empty min Optional.empty");
        check(Stream.of(3, 9, 1).max(Integer::compare).get() == 9, "max present");
        // distinct 用 equals 折叠（保留首个）
        java.util.List<String> dup = Stream.of("a", new String("a"), "b").distinct()
              .collect(Collectors.toList());
        check(dup.size() == 2 && dup.get(0).equals("a"), "distinct folds by equals");
        // Stream.of((T[]) null) NPE vs of(null) 单元素
        checkThrows(NullPointerException.class, () -> Stream.of((Integer[]) null), "of(null array) NPE");
        check(Stream.of((Object) null).count() == 1, "of(null scalar) single element");
        // builder 复用 ISE + 空流
        Stream.Builder<Integer> b = Stream.builder();
        b.add(1);
        b.build();
        checkThrows(IllegalStateException.class, () -> b.add(2), "builder reuse ISE");
        checkThrows(IllegalStateException.class, () -> b.build(), "builder rebuild ISE");
        check(Stream.builder().build().count() == 0, "empty builder empty stream");
        // onClose 异常聚合：主异常 + suppressed
        RuntimeException e1 = new RuntimeException("one");
        RuntimeException e2 = new RuntimeException("two");
        Stream<Integer> cs = Stream.of(1).onClose(() -> { throw e1; }).onClose(() -> { throw e2; });
        try {
            cs.close();
            check(false, "close throws primary");
        } catch (RuntimeException ex) {
            check(ex.getMessage().equals("one"), "primary is first throwing handler");
            Throwable[] sup = ex.getSuppressed();
            check(sup.length == 1 && sup[0] == e2, "second handler exception suppressed");
        }
        // 顺序不变量：filter+map+sorted+collect 与顺序手算一致（parallel 全等）
        java.util.List<Integer> ref = Stream.of(5, 3, 8, 1, 9, 2).filter(x -> x % 2 == 1)
                .sorted().collect(Collectors.toList());
        java.util.List<Integer> par = Stream.of(5, 3, 8, 1, 9, 2).parallel().filter(x -> x % 2 == 1)
                .sorted().collect(Collectors.toList());
        check(ref.equals(par) && ref.equals(Arrays.asList(1, 3, 5, 9)), "parallel == sequential for sorted collect");
        // iterate + generate 确定性
        check(Stream.iterate(1, i -> i * 2).limit(6).collect(Collectors.toList())
              .equals(Arrays.asList(1, 2, 4, 8, 16, 32)), "iterate doubles");
        final int[] g = {0};
        check(Stream.generate(() -> ++g[0]).limit(3).collect(Collectors.toList())
              .equals(Arrays.asList(1, 2, 3)), "generate with limit");
        // toArray(IntFunction) 空流零长
        check(Stream.<String>empty().toArray(String[]::new).length == 0, "empty toArray zero length");
        String[] typed = Stream.of("x", "y").toArray(String[]::new);
        check(typed.length == 2 && typed.getClass() == String[].class, "typed array exact class");
    }

    public static void main(String[] args) {
        testContracts();
        testFactories();
        testLaziness();
        testIntermediateOps();
        testTerminalReduce();
        testTerminalCollect();
        testTerminalMatchMinMax();
        testToArray();
        testSingleUse();
        testParallelConsistency();
        testNullAndBadArgs();
        testOnClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
