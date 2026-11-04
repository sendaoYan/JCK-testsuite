/*
 * @test
 * @summary java.util.function.Consumer contract: accept and andThen ordering, side effects, null->NPE.
 * @run main ConsumerTest
 */

import java.util.function.Consumer;

public class ConsumerTest {
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
        final int[] log = new int[1];
        Consumer<Integer> inc = x -> { log[0] += x; };
        inc.accept(5);
        check(log[0] == 5, "accept");
        inc.accept(-2);
        check(log[0] == 3, "accept negative");
        Consumer<Integer> dec = x -> { log[0] -= x; };
        inc.andThen(dec).accept(10);
        check(log[0] == 3, "andThen: inc then dec -> unchanged");
        checkThrows(NullPointerException.class, () -> inc.andThen(null), "andThen(null)");
        final int[] order = new int[1];
        Consumer<Integer> first = x -> { check(order[0] == 0, "first"); order[0] = 1; };
        Consumer<Integer> second = x -> { check(order[0] == 1, "second"); order[0] = 2; };
        first.andThen(second).accept(0);
        check(order[0] == 2, "order completed");
    }

    // ===================== 契约深化：andThen 顺序/异常中继/链式/null 元素 =====================
    static void testContracts() {
        final StringBuilder order = new StringBuilder();
        Consumer<String> a = s -> order.append("a").append(s);
        Consumer<String> b = s -> order.append("b").append(s);
        a.andThen(b).accept("x");
        check(order.toString().equals("axbx"), "andThen order");
        order.setLength(0);
        a.andThen(b).andThen(a).accept("y");
        check(order.toString().equals("aybyay"), "3-stage chain order");
        final int[] ran = {0};
        RuntimeException boom = new RuntimeException("c");
        try {
            ((Consumer<String>) s -> { throw boom; }).andThen(s -> ran[0]++).accept("z");
            check(false, "exception relayed");
        } catch (RuntimeException e) {
            check(e == boom, "exact exception relayed");
        }
        check(ran[0] == 0, "second not run after first throws");
        checkThrows(NullPointerException.class, () -> a.andThen(null), "andThen(null) NPE");
        final boolean[] sawNull = {false};
        Consumer<String> nullOk = s -> sawNull[0] = s == null;
        nullOk.accept(null);
        check(sawNull[0], "null element acceptable");
        // andThen 返回新对象（非自身）
        check(a.andThen(b) != a && a.andThen(b) != b, "andThen returns composed instance");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
