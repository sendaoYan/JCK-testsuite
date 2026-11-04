/*
 * @test
 * @summary java.util.function.BiConsumer contract: accept and andThen ordering, side effects, null->NPE.
 * @run main BiConsumerTest
 */

import java.util.function.BiConsumer;

public class BiConsumerTest {
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
        final int[] acc = new int[1];
        BiConsumer<Integer, Integer> add = (a, b) -> { acc[0] += a * b; };
        add.accept(2, 3);
        check(acc[0] == 6, "accept");
        add.accept(0, 100);
        check(acc[0] == 6, "accept zero");
        BiConsumer<Integer, Integer> sub = (a, b) -> { acc[0] -= a * b; };
        add.andThen(sub).accept(2, 3);
        check(acc[0] == 6, "andThen add then sub -> unchanged");
        checkThrows(NullPointerException.class, () -> add.andThen(null), "andThen(null)");
        final int[] order = new int[1];
        add.andThen((a, b) -> { check(order[0] == 0, "first"); order[0] = 1; }).accept(0, 0);
        check(order[0] == 1, "order");
    }

    // ===================== 契约深化：andThen 执行顺序矩阵/异常中继/链式 =====================
    static void testContracts() {
        final StringBuilder order = new StringBuilder();
        BiConsumer<String, Integer> first = (s, n) -> order.append("f").append(s).append(n);
        BiConsumer<String, Integer> second = (s, n) -> order.append("s").append(s).append(n);
        first.andThen(second).accept("x", 1);
        check(order.toString().equals("fx1sx1"), "andThen runs first then second, same args: " + order);
        // 三段链保持顺序
        order.setLength(0);
        BiConsumer<String, Integer> third = (s, n) -> order.append("t");
        first.andThen(second).andThen(third).accept("y", 2);
        check(order.toString().equals("fy2sy2t"), "3-stage andThen order");
        // 第一个抛异常 → 第二个不执行，异常对象透传
        final int[] ran = {0};
        RuntimeException boom = new RuntimeException("boom");
        BiConsumer<String, Integer> thrower = (s, n) -> { throw boom; };
        try {
            thrower.andThen((s, n) -> ran[0]++).accept("z", 3);
            check(false, "exception relayed");
        } catch (RuntimeException e) {
            check(e == boom, "exact exception object relayed");
        }
        check(ran[0] == 0, "second consumer not run when first throws");
        checkThrows(NullPointerException.class, () -> first.andThen(null), "andThen(null) NPE");
        // null 元素可被消费
        final boolean[] sawNull = {false};
        BiConsumer<String, String> nullOk = (a, b) -> sawNull[0] = (a == null && b == null);
        nullOk.accept(null, null);
        check(sawNull[0], "null arguments acceptable");
        // 匿名类实现
        BiConsumer<Integer, Integer> anon = new BiConsumer<Integer, Integer>() {
            public void accept(Integer a, Integer b) { order.append(a + b); }
        };
        order.setLength(0);
        anon.accept(2, 3);
        check(order.toString().equals("5"), "anonymous class impl");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
