/*
 * @test
 * @summary java.util.concurrent.CancellationException conformance: white-box structure + black-box behavior.
 * @run main CancellationExceptionTest
 */

import java.util.concurrent.*;

public class CancellationExceptionTest {
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
        Class<?> c = cls("java.util.concurrent.CancellationException");
        check(c.getSuperclass() == IllegalStateException.class, "extends IllegalStateException");
        hasCtor(c);
        hasCtor(c, String.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CancellationException e = new CancellationException("m");
        check(e.getMessage().equals("m"), "message from (String) ctor");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
        CancellationException e0 = new CancellationException();
        check(e0.getMessage() == null, "no-arg ctor null message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
