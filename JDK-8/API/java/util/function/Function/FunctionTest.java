/*
 * @test
 * @summary java.util.function.Function contract: apply, andThen/compose ordering, identity, null->NPE, side-effect invocation, exception relay.
 * @run main FunctionTest
 */

import java.util.function.Function;

public class FunctionTest {
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
        Function<String, Integer> len = String::length;
        check(len.apply("abc") == 3, "apply");
        check(len.apply("") == 0, "apply empty");
        checkThrows(NullPointerException.class, () -> len.apply(null), "String.length(null)");
        Function<Integer, Integer> square = x -> x * x;
        check(len.andThen(square).apply("abcd") == 16, "andThen g(f(x))");
        check("7".equals(len.andThen(Integer::toHexString).apply("1234567")), "andThen hex 7");
        Function<Integer, Integer> lenOfDigits = len.compose(String::valueOf);
        check(lenOfDigits.apply(12345) == 5, "compose f(g(x))");
        check(lenOfDigits.apply(-123) == 4, "compose negative");
        Object o = new Object();
        check(Function.identity().apply(o) == o, "identity same reference");
        check(Function.identity().apply(null) == null, "identity(null)");
        checkThrows(NullPointerException.class, () -> len.andThen(null), "andThen(null)");
        checkThrows(NullPointerException.class, () -> len.compose(null), "compose(null)");
        final int[] calls = new int[1];
        Function<Integer,Integer> inner = x -> { calls[0]++; return x + 1; };
        Function<Integer,Integer> outer = x -> x * 10;
        Function<Integer,Integer> composed = outer.compose(inner);
        check(calls[0] == 0, "compose lazy");
        check(composed.apply(1) == 20, "composed result");
        check(calls[0] == 1, "inner called once");
        IllegalStateException ex = new IllegalStateException("boom");
        Function<Integer,Integer> fail = x -> { throw ex; };
        try { fail.andThen(x -> x + 1).apply(1); throw new AssertionError("relay"); }
        catch (IllegalStateException c) { check(c == ex, "relay andThen"); }
        try { fail.compose((Integer x) -> x).apply(1); throw new AssertionError("relay2"); }
        catch (IllegalStateException c) { check(c == ex, "relay compose"); }
    }

    // ===================== 契约深化：compose vs andThen 顺序矩阵/identity 引用透传/异常中继 =====================
    static void testContracts() {
        final StringBuilder order = new StringBuilder();
        Function<Integer, Integer> dbl = x -> { order.append('d'); return x * 2; };
        Function<Integer, Integer> inc = x -> { order.append('i'); return x + 1; };
        order.setLength(0);
        check(dbl.andThen(inc).apply(3) == 7 && order.toString().equals("di"), "andThen d,i");
        order.setLength(0);
        check(dbl.compose(inc).apply(3) == 8 && order.toString().equals("id"), "compose i,d");
        order.setLength(0);
        check(dbl.compose(inc).apply(3) == inc.andThen(dbl).apply(3), "compose(g) == g.andThen(f)");
        Function<String, String> id = Function.identity();
        check(id.apply("x") == "x", "identity returns same reference");
        Object o = new Object();
        check(Function.identity().apply(o) == o, "identity reference equality for any type");
        checkThrows(NullPointerException.class, () -> dbl.andThen(null), "andThen(null) NPE");
        checkThrows(NullPointerException.class, () -> dbl.compose(null), "compose(null) NPE");
        RuntimeException boom = new RuntimeException("fn");
        try {
            ((Function<Integer, Integer>) x -> { throw boom; }).andThen(inc).apply(1);
            check(false, "exception relayed");
        } catch (RuntimeException e) {
            check(e == boom, "exact exception relayed");
        }
        // 类型变换链 String->Integer->String
        Function<String, Integer> len = String::length;
        Function<Integer, String> quoted = n -> "[" + n + "]";
        check(len.andThen(quoted).apply("abcd").equals("[4]"), "type-changing chain");
        check(len.compose(s -> s + "xy").apply("ab") == 4, "compose type-changing");
        // null 入参合法（函数自行处理）
        Function<String, String> nullSafe = s -> s == null ? "was-null" : s;
        check("was-null".equals(nullSafe.apply(null)), "null input handled by function");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
