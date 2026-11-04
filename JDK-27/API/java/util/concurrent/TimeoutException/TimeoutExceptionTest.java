/*
 * @test
 * @summary java.util.concurrent.TimeoutException conformance: white-box structure + black-box behavior.
 * @run main TimeoutExceptionTest
 */

import java.util.concurrent.*;

public class TimeoutExceptionTest {
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
        Class<?> c = cls("java.util.concurrent.TimeoutException");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        hasCtor(c);
        hasCtor(c, String.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        TimeoutException e = new TimeoutException("m");
        check(e.getMessage().equals("m"), "message");
        check(e instanceof Exception, "instanceof Exception");
        check(new TimeoutException().getMessage() == null, "no-arg null message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
