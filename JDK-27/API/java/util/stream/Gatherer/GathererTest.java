/*
 * @test
 * @summary java.util.stream.Gatherer conformance: white-box structure + black-box behavior.
 * @run main GathererTest
 */

import java.util.Arrays;
import java.util.List;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

public class GathererTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void expectThrows(Class<? extends Throwable> ex, Runnable r, String msg) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Gatherer");
        check(c.isInterface(), "Gatherer is an interface");
        boolean integ = Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("integrator")
                        && java.lang.reflect.Modifier.isAbstract(m.getModifiers()));
        check(integ, "integrator() is the single abstract method");
        for (String n : new String[]{"initializer", "combiner", "finisher", "andThen"}) {
            boolean found = Arrays.stream(c.getDeclaredMethods()).anyMatch(m -> m.getName().equals(n));
            check(found, "default method " + n + "() present");
        }
        for (String n : new String[]{"defaultInitializer", "defaultCombiner", "defaultFinisher"}) {
            boolean found = Arrays.stream(c.getDeclaredMethods()).anyMatch(m -> m.getName().equals(n));
            check(found, "static " + n + "() present");
        }
        long ofCount = Arrays.stream(c.getDeclaredMethods())
                .filter(m -> m.getName().equals("of") || m.getName().equals("ofSequential")).count();
        check(ofCount == 7, "of()/ofSequential() overloads present (7 total)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // stateless identity gatherer via of(Integrator)
        Gatherer<Integer, Void, Integer> identity =
                Gatherer.of((Void state, Integer e, Gatherer.Downstream<? super Integer> d) -> d.push(e));
        check(identity.initializer().get() == null, "default initializer yields null (Void state)");
        check(identity.combiner() != null && identity.finisher() != null, "default combiner/finisher non-null");
        check(Stream.of(1, 2, 3).gather(identity).toList().equals(List.of(1, 2, 3)),
              "identity gather passes elements through");

        // stateful ofSequential: count and emit once in finisher
        Gatherer<Integer, int[], Integer> counter = Gatherer.ofSequential(
                () -> new int[1],
                (int[] state, Integer e, Gatherer.Downstream<? super Integer> d) -> { state[0]++; return true; },
                (int[] state, Gatherer.Downstream<? super Integer> d) -> d.push(state[0]));
        check(Stream.of(1, 2, 3, 4).gather(counter).toList().equals(List.of(4)),
              "stateful ofSequential counts and finishes once");

        // gatherer that duplicates each element and appends a sentinel in finisher
        Gatherer<Integer, int[], Integer> dup = Gatherer.ofSequential(
                () -> new int[1],
                (int[] st, Integer e, Gatherer.Downstream<? super Integer> d) -> {
                    d.push(e); d.push(e); return true;
                },
                (int[] st, Gatherer.Downstream<? super Integer> d) -> d.push(-1));
        check(Stream.of(1, 2).gather(dup).toList().equals(List.of(1, 1, 2, 2, -1)),
              "multi-emission plus finisher sentinel");

        // andThen composition
        Gatherer<Integer, ?, Integer> plusOne =
                Gatherer.of((Void s, Integer e, Gatherer.Downstream<? super Integer> d) -> d.push(e + 1));
        Gatherer<Integer, ?, Integer> timesTwo =
                Gatherer.of((Void s, Integer e, Gatherer.Downstream<? super Integer> d) -> d.push(e * 2));
        check(Stream.of(1, 2, 3).gather(plusOne.andThen(timesTwo)).toList().equals(List.of(4, 6, 8)),
              "andThen composes gatherers ((e+1)*2)");

        // integrator can stop short by returning false
        Gatherer<Integer, int[], Integer> firstTwo = Gatherer.ofSequential(
                () -> new int[1],
                (int[] st, Integer e, Gatherer.Downstream<? super Integer> d) -> {
                    d.push(e);
                    st[0]++;
                    return st[0] < 2;
                });
        check(Stream.of(1, 2, 3, 4, 5).gather(firstTwo).toList().equals(List.of(1, 2)),
              "integrator returning false short-circuits input");
    }


    // ===================== 契约深化：null 契约/并行组合器合并/复用性/无限流短路（JDK-27 侧锁定） =====================
    static void testContracts() {
        expectThrows(NullPointerException.class, () -> Stream.of(1).gather(null), "gather(null) NPE");
        expectThrows(NullPointerException.class, () -> Gatherer.of(null), "Gatherer.of(null) NPE");
        expectThrows(NullPointerException.class, () -> Gatherer.ofSequential(null,
                (java.lang.Void s, Integer e, Gatherer.Downstream<? super Integer> d) -> true),
                "ofSequential(null initializer) NPE");
        // 无限流 + takeWhile 短路：gatherer 惰性
        check(Stream.iterate(1, i -> i + 1)
                .gather(Gatherer.of((java.lang.Void s, Integer e, Gatherer.Downstream<? super Integer> d) -> d.push(e * 2)))
                .takeWhile(x -> x < 7).toList().equals(List.of(2, 4, 6)),
              "gatherer is lazy: infinite source + takeWhile terminates");
        // 并行 gather：ofSequential 状态在 combiner 中合并（求和确定性）
        Gatherer<Integer, int[], Integer> summer = Gatherer.ofSequential(
                () -> new int[1],
                (int[] s, Integer e, Gatherer.Downstream<? super Integer> d) -> { s[0] += e; return true; },
                (int[] s, Gatherer.Downstream<? super Integer> d) -> d.push(s[0]));
        check(Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).parallel().gather(summer)
              .mapToInt(Integer::intValue).sum() == 55,
              "parallel gather merges state via combiner (total 55)");
        // gatherer 可复用（两个流各自独立）
        Gatherer<Integer, java.lang.Void, Integer> dbl =
                Gatherer.of((java.lang.Void s, Integer e, Gatherer.Downstream<? super Integer> d) -> d.push(e * 2));
        check(Stream.of(1, 2).gather(dbl).toList().equals(List.of(2, 4))
              && Stream.of(3).gather(dbl).toList().equals(List.of(6)),
              "gatherer reusable across streams");
        // gather 后流仍是 Stream：可继续链式操作
        Gatherer<String, java.lang.Void, Integer> len =
                Gatherer.of((java.lang.Void s, String e, Gatherer.Downstream<? super Integer> d) -> d.push(e.length()));
        check(Stream.of("a", "bb").gather(len).mapToInt(Integer::intValue).sum() == 3,
              "gather composes with downstream mapToInt");
        // 并行 == 顺序（无状态 gatherer + 排序）
        check(Stream.of(5, 3, 8, 1).parallel().gather(dbl).sorted().toList()
              .equals(Stream.of(5, 3, 8, 1).gather(dbl).sorted().toList()),
              "parallel gather == sequential gather (stateless + sorted)");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
