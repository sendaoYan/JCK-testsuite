/*
 * @test
 * @summary java.util.function.ToIntFunction contract: apply extracts primitive from object.
 * @run main ToIntFunctionTest
 */

import java.util.function.ToIntFunction;

public class ToIntFunctionTest {
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
        ToIntFunction<String> len = String::length;
        check(len.applyAsInt("abc") == 3, "apply");
        check(len.applyAsInt("") == 0, "apply empty");
        ToIntFunction<Object> hash = Object::hashCode;
        check(hash.applyAsInt(new Object()) != 0, "apply hashCode");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
