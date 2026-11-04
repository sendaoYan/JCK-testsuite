/*
 * @test
 * @summary java.util.TooManyListenersException API conformance: constructors and inheritance.
 * @run main TooManyListenersExceptionTest
 */

import java.util.TooManyListenersException;

public class TooManyListenersExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTooManyListenersException() {
        TooManyListenersException e = new TooManyListenersException("msg");
        check(e instanceof Exception, "extends Exception");
        check("msg".equals(e.getMessage()), "message from (String) ctor");
        check(new TooManyListenersException().getMessage() == null, "no-arg ctor has null message");
    }


    // ===================== 契约深化：受检异常语义/cause 缺省 =====================
    static void testTooManyContracts() {
        check(Exception.class.isAssignableFrom(TooManyListenersException.class)
              && !RuntimeException.class.isAssignableFrom(TooManyListenersException.class),
              "checked Exception (not RuntimeException)");
        check(new TooManyListenersException().getMessage() == null, "no-arg null message");
        check("one only".equals(new TooManyListenersException("one only").getMessage()),
              "message ctor");
        check(new TooManyListenersException("m").getCause() == null, "no cause by default");
    }

    public static void main(String[] args) {
        testTooManyListenersException();
        testTooManyContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
