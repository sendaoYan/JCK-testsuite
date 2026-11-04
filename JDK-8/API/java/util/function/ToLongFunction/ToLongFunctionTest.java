/*
 * @test
 * @summary java.util.function.ToLongFunction contract: apply extracts primitive from object.
 * @run main ToLongFunctionTest
 */

import java.util.function.ToLongFunction;

public class ToLongFunctionTest {
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
        ToLongFunction<String> len = String::length;
        check(len.applyAsLong("abc") == 3, "apply");
        check(len.applyAsLong("") == 0, "apply empty");
        ToLongFunction<Object> hash = Object::hashCode;
        check(hash.applyAsLong(new Object()) != 0, "apply hashCode");
    }

    // ===================== 契约深化：精确映射/方法引用族/字符串哈希锁定 =====================
    static void testContracts() {
        ToLongFunction<String> hash64 = s -> s.hashCode() * 0x100000000L + s.length();
        check(hash64.applyAsLong("a") == 97L * 4294967296L + 1, "composed mapping exact");
        ToLongFunction<Integer> wide = Integer::longValue;
        check(wide.applyAsLong(Integer.MAX_VALUE) == 2147483647L, "widening");
        ToLongFunction<Long> id = Long::longValue;
        check(id.applyAsLong(Long.MIN_VALUE) == Long.MIN_VALUE, "MIN passthrough");
        ToLongFunction<Object> nullZero = o -> o == null ? 0L : 1L;
        check(nullZero.applyAsLong(null) == 0L, "null branch");
        final int[] calls = {0};
        ToLongFunction<Integer> counting = n -> { calls[0]++; return n; };
        counting.applyAsLong(1);
        counting.applyAsLong(2);
        counting.applyAsLong(3);
        check(calls[0] == 3, "invocation count");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
