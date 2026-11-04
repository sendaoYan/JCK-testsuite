/*
 * @test
 * @summary java.util.function.ObjLongConsumer contract: accept object plus primitive, side effects.
 * @run main ObjLongConsumerTest
 */

import java.util.function.ObjLongConsumer;

public class ObjLongConsumerTest {
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
        final StringBuilder sb = new StringBuilder();
        ObjLongConsumer<String> acc = (s, v) -> { sb.append(s).append(v); };
        acc.accept("v=", 7L);
        check("v=7".equals(sb.toString()), "accept");
        acc.accept("!", 7L);
        check("v=7!7".equals(sb.toString()), "accept second");
    }

    // ===================== 契约深化：双参精确捕获/null 对象/边界 long =====================
    static void testContracts() {
        final java.util.List<String> log = new java.util.ArrayList<String>();
        ObjLongConsumer<String> c = (s, n) -> log.add(s + ":" + n);
        c.accept("a", 9L);
        check(log.get(0).equals("a:9"), "both args captured");
        c.accept("b", Long.MAX_VALUE);
        check(log.get(1).endsWith(":9223372036854775807"), "MAX formats");
        final boolean[] sawNull = {false};
        ObjLongConsumer<String> nullOk = (s, n) -> sawNull[0] = s == null;
        nullOk.accept(null, 0L);
        check(sawNull[0], "null object arg acceptable");
        ObjLongConsumer<Integer> anon = new ObjLongConsumer<Integer>() {
            public void accept(Integer i, long n) { log.add(String.valueOf(i * n)); }
        };
        anon.accept(6, 7L);
        check(log.get(log.size() - 1).equals("42"), "anonymous impl");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
