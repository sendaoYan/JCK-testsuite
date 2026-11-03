/*
 * @test
 * @summary java.time.DateTimeException conformance: white-box hierarchy + black-box message/cause constructors.
 * @run main DateTimeExceptionTest
 */

import java.time.DateTimeException;

public class DateTimeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.DateTimeException");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.RuntimeException"), "extends RuntimeException");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, Throwable.class) != null, "ctor(String,Throwable)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DateTimeException e = new DateTimeException("msg");
        check(e instanceof RuntimeException, "is a RuntimeException");
        check("msg".equals(e.getMessage()), "(String) constructor");
        check(e.getCause() == null, "no cause by default");
        check(e.getStackTrace() != null, "getStackTrace non-null");

        Throwable cause = new IllegalArgumentException("cause");
        DateTimeException e2 = new DateTimeException("m2", cause);
        check("m2".equals(e2.getMessage()), "(String,Throwable) message");
        check(e2.getCause() == cause, "(String,Throwable) cause");
        check(e2 instanceof RuntimeException, "with-cause instance still RuntimeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
