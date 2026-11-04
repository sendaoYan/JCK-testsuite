/*
 * @test
 * @summary java.util.concurrent.StructuredTaskScope$CancelledByTimeoutException conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main CancelledByTimeoutExceptionTest
 */

import java.util.concurrent.*;

public class CancelledByTimeoutExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasCtor(Class<?> c, Class<?>... p) {
        try { c.getConstructor(p); } catch (NoSuchMethodException e) { throw new AssertionError("missing ctor on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.StructuredTaskScope$CancelledByTimeoutException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        hasCtor(c);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StructuredTaskScope.CancelledByTimeoutException e = new StructuredTaskScope.CancelledByTimeoutException();
        check(e instanceof RuntimeException, "instanceof RuntimeException");
        check(e.getMessage() == null, "no-arg ctor null message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
