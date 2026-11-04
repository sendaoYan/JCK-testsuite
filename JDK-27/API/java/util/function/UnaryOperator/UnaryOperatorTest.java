/*
 * @test
 * @summary java.util.function.UnaryOperator contract: identity and inherited Function composition.
 * @run main UnaryOperatorTest
 */

import java.util.function.Function;
import java.util.function.UnaryOperator;

public class UnaryOperatorTest {
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
    static void testMain() {
        UnaryOperator<Integer> inc = x -> x + 1;
        check(inc.apply(1) == 2, "apply");
        check(inc.andThen(x -> x * 10).apply(1) == 20, "andThen");
        check(inc.compose((Integer x) -> x * 10).apply(1) == 11, "compose");
        check(UnaryOperator.<Integer>identity().apply(42) == 42, "identity");
        checkThrows(NullPointerException.class, () -> inc.andThen(null), "andThen(null)");
        checkThrows(NullPointerException.class, () -> inc.compose(null), "compose(null)");
    }

    // ===================== 契约深化：Function 继承/compose+andThen 可用/Stream.iterate 驱动 =====================
    static void testContracts() {
        UnaryOperator<String> up = String::toUpperCase;
        check(up.apply("ab").equals("AB"), "apply");
        Function<String, String> asFn = up;  // UnaryOperator extends Function
        check(asFn.apply("cd").equals("CD"), "assignable to Function");
        check(up.compose(s -> s + "!").apply("x").equals("X!"), "compose inherited");
        check(up.andThen(s -> s + "!").apply("x").equals("X!"), "andThen inherited");
        checkThrows(NullPointerException.class, () -> up.andThen(null), "andThen(null) NPE");
        checkThrows(NullPointerException.class, () -> up.compose(null), "compose(null) NPE");
        // 恒等算子
        UnaryOperator<Object> id = x -> x;
        Object o = new Object();
        check(id.apply(o) == o, "identity reference passthrough");
        // Stream.iterate 固定 5 项（UnaryOperator 驱动）
        java.util.List<Integer> got = java.util.stream.Stream.iterate(1, n -> n * 2)
                .limit(5).collect(java.util.stream.Collectors.toList());
        check(got.equals(java.util.Arrays.asList(1, 2, 4, 8, 16)), "iterate doubles");
        // 匿名类
        UnaryOperator<Integer> anon = new UnaryOperator<Integer>() {
            public Integer apply(Integer n) { return n + 1; }
        };
        check(anon.apply(41) == 42, "anonymous impl");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
