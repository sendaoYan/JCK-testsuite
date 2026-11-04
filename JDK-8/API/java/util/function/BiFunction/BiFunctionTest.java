/*
 * @test
 * @summary java.util.function.BiFunction contract: apply and andThen ordering, null->NPE, exception relay.
 * @run main BiFunctionTest
 */

import java.util.function.Function;
import java.util.function.BiFunction;

public class BiFunctionTest {
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
        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
        check(add.apply(2, 3) == 5, "apply");
        check(add.apply(-1, 1) == 0, "apply zero");
        BiFunction<Integer, Integer, String> concat = add.andThen(Object::toString);
        check("5".equals(concat.apply(2, 3)), "andThen g(f(x))");
        checkThrows(NullPointerException.class, () -> add.andThen(null), "andThen(null)");
        final int[] calls = new int[1];
        BiFunction<Integer,Integer,Integer> lazy = add.andThen(x -> { calls[0]++; return x; });
        check(calls[0] == 0, "andThen lazy");
        check(lazy.apply(1, 2) == 3, "andThen result");
        check(calls[0] == 1, "called once");
        IllegalStateException ex = new IllegalStateException("boom");
        BiFunction<Integer,Integer,Integer> fail = (a, b) -> { throw ex; };
        try { fail.andThen(x -> x + 1).apply(1, 2); throw new AssertionError("relay"); }
        catch (IllegalStateException c) { check(c == ex, "relay"); }
    }

    // ===================== 契约深化：andThen 顺序/generic 协变/无 compose/异常中继 =====================
    static void testContracts() {
        final int[] calls = {0};
        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
        Function<Integer, String> tag = x -> { calls[0]++; return "v" + x; };
        calls[0] = 0;
        check(add.andThen(tag).apply(2, 3).equals("v5"), "andThen applies function to result");
        check(calls[0] == 1, "downstream called exactly once");
        checkThrows(NullPointerException.class, () -> add.andThen(null), "andThen(null) NPE");
        // BiFunction 无 compose（API 面锁定）
        try {
            check(BiFunction.class.getMethod("compose", Function.class) == null, "unreachable");
        } catch (NoSuchMethodException e) {
            check(true, "BiFunction declares no compose");
        }
        // 异常中继 + 下游不执行
        final int[] ran = {0};
        RuntimeException boom = new RuntimeException("bf");
        BiFunction<Integer, Integer, Integer> thrower = (a, b) -> { throw boom; };
        try {
            thrower.andThen(x -> { ran[0]++; return x; }).apply(1, 2);
            check(false, "exception relayed");
        } catch (RuntimeException e) {
            check(e == boom, "exact exception relayed");
        }
        check(ran[0] == 0, "downstream not run when upstream throws");
        // 返回 null 合法
        BiFunction<String, String, String> nuller = (a, b) -> null;
        check(nuller.apply("a", "b") == null, "null result legal");
        // 协变：子类型函数可组合
        BiFunction<Object, Object, CharSequence> wide = (a, b) -> String.valueOf(a) + b;
        check("xy".equals(wide.andThen((CharSequence s) -> s.toString()).apply("x", "y")), "covariant andThen");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
