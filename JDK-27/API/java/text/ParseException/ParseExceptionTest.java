/*
 * @test
 * @summary java.text.ParseException conformance: white-box hierarchy + black-box message/offset/cause chain.
 * @run main ParseExceptionTest
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ParseExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.ParseException");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "extends Exception");
        check(c.getConstructor(String.class, int.class) != null, "ctor(String,int)");
        check(c.getMethod("getErrorOffset") != null, "getErrorOffset");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ParseException e = new ParseException("bad", 5);
        check("bad".equals(e.getMessage()), "message");
        check(e.getErrorOffset() == 5, "error offset");
        check(e instanceof Exception, "is checked Exception");
        check(!(((Object) e) instanceof RuntimeException), "not unchecked");
        check(e.getStackTrace() != null, "getStackTrace non-null");

        // cause chain via Throwable.initCause
        Exception cause = new IllegalStateException("root");
        ParseException e2 = new ParseException("wrapped", 3);
        e2.initCause(cause);
        check(e2.getCause() == cause, "initCause/getCause");
        check(e2.getErrorOffset() == 3, "error offset preserved with cause");

        boolean thrown = false;
        try {
            new SimpleDateFormat("yyyy").parse("not-a-year");
        } catch (ParseException ex) {
            thrown = true;
            check(ex.getErrorOffset() >= 0, "thrown error offset non-negative");
        }
        check(thrown, "SimpleDateFormat.parse throws ParseException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
